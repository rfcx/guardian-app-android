package org.rfcx.ranger.view.alert

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.crashlytics.android.Crashlytics
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event

class AlertBottomDialogViewModel(private val context: Context) : ViewModel() {
	
	
	private var _event: MutableLiveData<Event> = MutableLiveData()
	val event: LiveData<Event>
		get() = _event
	
	private var _spectrogramImage: MutableLiveData<String> = MutableLiveData()
	val spectrogramImage: LiveData<String>
		get() = _spectrogramImage
	
	private var _eventState: MutableLiveData<EventState> = MutableLiveData()
	val eventState: LiveData<EventState>
		get() = _eventState
	
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
	
	
	init {
		_playerState.value = Player.STATE_IDLE
	}
	
	fun setEvent(event: Event) {
		this._event.value = event
		setSpectrogramImage()
		this._eventState.value = EventState.NONE
		event.audio?.opus?.let {
			initPlayer(it)
		}
	}
	
	private fun setSpectrogramImage() {
		_spectrogramImage.value = "https://assets.rfcx.org/audio/${event.value?.audioGUID}.png?width=512&height=256" +
				"&offset=${0}&duration=${90L * 1000}"
	}
	
	
	fun replaySound() {
		if (exoPlayer.playbackState == Player.STATE_ENDED) {
			exoPlayer.seekTo(0)
			exoPlayer.playWhenReady = true
		} else {
			event.value?.audio?.opus?.let {
				initPlayer(it)
			}
		}
	}
	
	fun seekPlayerTo(timeMs: Long) {
		if (exoPlayer.playbackState != Player.STATE_IDLE) {
			exoPlayer.seekTo(timeMs)
			exoPlayer.playWhenReady = true
		}
	}
	
	private fun initPlayer(audioUrl: String) {
		
		val thisAudioUrl: String = if (Build.VERSION.SDK_INT < 21) {
			audioUrl.replace("https://assets.rfcx.org/", "http://api-insecure.rfcx.org/v1/assets/")
		} else {
			audioUrl
		}
		val descriptorFactory =
				DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getString(R.string.app_name)))
		
		val mediaSource = ExtractorMediaSource.Factory(descriptorFactory).createMediaSource(Uri.parse(thisAudioUrl))
		exoPlayer.playWhenReady = true
		exoPlayer.prepare(mediaSource)
		exoPlayer.addListener(exoPlayerListener)
		_playerState.value = Player.STATE_BUFFERING
	}
	
	
	private val exoPlayerListener = object : Player.EventListener {
		override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
			_playerState.value = playbackState
			when (playbackState) {
				Player.STATE_READY -> {
					playerTimeHandler.postDelayed(playerTimeRunnable, delayTime)
				}
				
				Player.STATE_ENDED -> {
					playerTimeHandler.removeCallbacks(playerTimeRunnable)
				}
			}
		}
		
		override fun onPlayerError(error: ExoPlaybackException?) {
			_playerError.value = error
			Crashlytics.logException(error)
		}
	}
	
	
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
	
	companion object {
		const val maxProgress = 100_000
		private const val delayTime = 100L
	}
	
}

enum class EventState {
	NONE, REVIEWED
}