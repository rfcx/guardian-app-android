package org.rfcx.incidents.view.alert

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
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.R
import org.rfcx.incidents.data.local.AlertDb
import org.rfcx.incidents.entity.alert.Alert
import org.rfcx.incidents.service.CleanupAudioCacheWorker.Companion.AUDIOS_SUB_DIRECTORY
import org.rfcx.incidents.util.toIsoFormatString
import java.io.File

class AlertDetailViewModel(private val context: Context, private val alertDb: AlertDb) : ViewModel() {
	var _alert: Alert? = null
		private set
	
	fun setAlert(alert: Alert) {
		_alert = alert
		initPlayer(setFormatAudio(alert))
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
		
		val audiosDirectory = File(context.cacheDir, AUDIOS_SUB_DIRECTORY)
		val audioFile = File(audiosDirectory, setFormatAudio(_alert!!))
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
	
	fun setFormatAudio(alert: Alert): String {
		return "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
//		return "${BuildConfig.RANGER_API_DOMAIN}media/${alert.streamId}_t${alert.start.toIsoFormatString()}.${alert.end.toIsoFormatString()}_rfull_g1_fmp3"
	}
	
	fun setFormatUrlOfSpectrogram(alert: Alert): String {
		return "${BuildConfig.RANGER_API_DOMAIN}media/${alert.streamId}_t${alert.start.toIsoFormatString()}.${alert.end.toIsoFormatString()}_rfull_g1_fspec_d600.512_wdolph_z120.png"
	}
	
	companion object {
		const val maxProgress = 100_000
		private const val delayTime = 100L
		const val confidenceValue = 0.8
	}
}
