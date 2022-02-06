package org.rfcx.incidents.view.events.detail

import android.content.Context
import android.net.Uri
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.observers.DisposableSingleObserver
import okhttp3.ResponseBody
import okio.BufferedSink
import okio.buffer
import okio.sink
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.R
import org.rfcx.incidents.data.local.AlertDb
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.GetDetectionsUseCase
import org.rfcx.incidents.domain.MediaUseCase
import org.rfcx.incidents.entity.event.Alert
import org.rfcx.incidents.entity.event.DetectionFactory
import org.rfcx.incidents.entity.event.Detections
import org.rfcx.incidents.entity.event.Confidence
import org.rfcx.incidents.util.toIsoFormatString
import java.io.File
import java.io.IOException

class AlertDetailViewModel(
    private val context: Context,
    private val alertDb: AlertDb,
    private val mediaUseCase: MediaUseCase,
    private val getDetectionsUseCase: GetDetectionsUseCase
) : ViewModel() {
    var _alert: Alert? = null
        private set

    private var _classifiedCation: MutableLiveData<Result<List<Confidence>>> = MutableLiveData()
    val classifiedCation: LiveData<Result<List<Confidence>>> get() = _classifiedCation

    fun setAlert(alert: Alert) {
        _alert = alert
        getAudio(alert)
    }

    private val exoPlayer by lazy { ExoPlayerFactory.newSimpleInstance(context) }
    private var _playerState: MutableLiveData<Int> = MutableLiveData()
    val playerState: LiveData<Int>
        get() = _playerState

    private var _playerError: MutableLiveData<ExoPlaybackException> = MutableLiveData()
    val playerError: LiveData<ExoPlaybackException?>
        get() = _playerError

    private var _playerProgress: MutableLiveData<Int> = MutableLiveData()
    val playerProgress: LiveData<Int>
        get() = _playerProgress

    private var _loadAudioError: MutableLiveData<String> = MutableLiveData()
    val loadAudioError: LiveData<String?>
        get() = _loadAudioError

    private val playerTimeHandler: Handler = Handler()
    private val playerTimeRunnable = object : Runnable {
        override fun run() {
            updateSoundProgress()
            playerTimeHandler.postDelayed(this, delayTime)
        }
    }

    private fun updateSoundProgress() {
        exoPlayer.let {
            val duration = it.duration
            val currentDuration = it.currentPosition
            val progress = maxProgress * currentDuration / duration
            _playerProgress.value = progress.toInt()
        }
    }

    fun getDuration(): Long = exoPlayer.duration / 1000L

    override fun onCleared() {
        super.onCleared()
        playerTimeHandler.removeCallbacks(playerTimeRunnable)
        try {
            exoPlayer.removeListener(exoPlayerListener)
            exoPlayer.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val exoPlayerListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            _playerState.value = playbackState
            when (playbackState) {
                Player.STATE_READY -> {
                    playerTimeHandler.removeCallbacks(playerTimeRunnable)
                    playerTimeHandler.postDelayed(playerTimeRunnable, delayTime)
                }

                Player.STATE_ENDED -> {
                    playerTimeHandler.removeCallbacks(playerTimeRunnable)
                }
            }
        }

        override fun onPlayerError(error: ExoPlaybackException?) {
            FirebaseCrashlytics.getInstance().log(error?.message.toString())
            _playerError.value = error
        }
    }

    init {
        _playerState.value = Player.STATE_BUFFERING
    }

    fun replaySound(url: String) {
        if (exoPlayer.playbackState == Player.STATE_ENDED) {
            exoPlayer.seekTo(0)
            exoPlayer.playWhenReady = true
        } else {
            initPlayer(url)
        }
    }

    fun seekPlayerTo(timeMs: Long) {
        if (exoPlayer.playbackState != Player.STATE_IDLE) {
            exoPlayer.seekTo(timeMs)
            exoPlayer.playWhenReady = true
        }
    }

    private fun initPlayer(audioUrl: String) {
        if (_alert == null) return

        val descriptorFactory =
            DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getString(R.string.app_name)))
        val audioFile = File(this.context.getExternalFilesDir(null).toString(), audioUrl)
        val mediaSource = if (audioFile.exists()) {
            ExtractorMediaSource.Factory(descriptorFactory).createMediaSource(Uri.fromFile(audioFile))
        } else {
            ExtractorMediaSource.Factory(descriptorFactory).createMediaSource(Uri.parse(audioUrl))
        }

        exoPlayer.playWhenReady = true
        exoPlayer.prepare(mediaSource)
        exoPlayer.addListener(exoPlayerListener)
        _playerState.value = Player.STATE_BUFFERING
    }

    fun getAlert(coreId: String): Alert? = alertDb.getAlert(coreId)

    private fun getAudio(alert: Alert) {
        val fileName =
            alert.streamId + "_t" + alert.start.toIsoFormatString() + "." + alert.end.toIsoFormatString() + "_rfull_g1_fmp3.mp3"
        mediaUseCase.execute(
            object : DisposableSingleObserver<ResponseBody>() {
                override fun onSuccess(t: ResponseBody) {
                    saveFile(t, fileName) {
                        if (it) {
                            initPlayer(fileName)
                            getDetections(alert)
                        }
                    }
                }

                override fun onError(e: Throwable) {
                    _loadAudioError.value = e.message
                    e.printStackTrace()
                }
            },
            fileName
        )
    }

    private fun saveFile(response: ResponseBody, fileName: String, callback: (Boolean) -> Unit) {
        val temp = File(this.context.getExternalFilesDir(null).toString(), "temp_$fileName")
        val file = File(this.context.getExternalFilesDir(null).toString(), fileName)

        if (file.exists()) {
            callback.invoke(true)
            return
        }

        try {
            val sink: BufferedSink = temp.sink().buffer()
            sink.writeAll(response.source())
            sink.close()
            temp.renameTo(file)
            callback.invoke(true)
        } catch (e: IOException) {
            e.printStackTrace()
            callback.invoke(false)
        }
    }

    fun getDetections(alert: Alert) {
        getDetectionsUseCase.execute(
            object : DisposableSingleObserver<List<Detections>>() {
                override fun onSuccess(t: List<Detections>) {
                    val confidence = t.map { checkSpanOfBox(alert, it) }
                    _classifiedCation.value = Result.Success(confidence)
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }
            },
            DetectionFactory(
                alert.streamId, alert.start.time, alert.end.time,
                listOf(
                    alert.classification?.value
                        ?: ""
                )
            )
        )
    }

    fun checkSpanOfBox(alert: Alert, detections: Detections): Confidence {
        val startSpan = (detections.start.time - alert.start.time) / 1000L
        val endSpan = (detections.start.time - alert.start.time) / 1000L
        return if (startSpan == endSpan) {
            if (endSpan == getDuration()) {
                Confidence(
                    ((detections.start.time - alert.start.time) / 1000L) - 1,
                    detections.confidence,
                    (detections.end.time - alert.start.time) / 1000L
                )
            } else {
                Confidence(
                    (detections.start.time - alert.start.time) / 1000L,
                    detections.confidence,
                    ((detections.end.time - alert.start.time) / 1000L) + 1
                )
            }
        } else {
            Confidence(
                (detections.start.time - alert.start.time) / 1000L,
                detections.confidence,
                (detections.end.time - alert.start.time) / 1000L
            )
        }
    }

    fun setFormatUrlOfSpectrogram(alert: Alert): String {
        val filename = "${alert.streamId}_t${alert.start.toIsoFormatString()}.${alert.end.toIsoFormatString()}_rfull_g1_fspec_d600.512_wdolph_z120.png"
        return "${BuildConfig.RANGER_API_BASE_URL}media/$filename"
    }

    fun setFormatUrlOfAudio(alert: Alert): String {
        val filename = "${alert.streamId}_t${alert.start.toIsoFormatString()}.${alert.end.toIsoFormatString()}_rfull_g1_fmp3.mp3"
        return "${BuildConfig.RANGER_API_BASE_URL}media/$filename"
    }

    companion object {
        const val maxProgress = 100_000
        private const val delayTime = 100L
    }
}
