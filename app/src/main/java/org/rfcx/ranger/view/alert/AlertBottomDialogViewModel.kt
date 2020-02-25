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
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.R
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.domain.alert.GetEventUseCase
import org.rfcx.ranger.data.remote.domain.alert.ReviewEventUseCase
import org.rfcx.ranger.entity.event.Confidence
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventReview
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.service.ReviewEventSyncWorker
import org.rfcx.ranger.util.NetworkNotConnection
import org.rfcx.ranger.util.getNameEmail
import org.rfcx.ranger.util.isNetworkAvailable
import java.io.File

class AlertBottomDialogViewModel(private val context: Context,
                                 private val reviewEventUseCase: ReviewEventUseCase,
                                 private val eventDb: EventDb,
                                 private val getEventUseCase: GetEventUseCase) : ViewModel() {
	var eventResult: Event? = null
		private set
	private var _event: MutableLiveData<Result<Event>> = MutableLiveData()
	val event: LiveData<Result<Event>>
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
	
	private var _classifiedCation: MutableLiveData<Result<List<Confidence>>> = MutableLiveData()
	val classifiedCation: LiveData<Result<List<Confidence>>>
		get() = _classifiedCation
	
	private var _reviewEvent: MutableLiveData<Result<Pair<Event, ReviewEventFactory>>> = MutableLiveData()
	val reviewEvent: LiveData<Result<Pair<Event, ReviewEventFactory>>>
		get() = _reviewEvent
	
	init {
		_playerState.value = Player.STATE_BUFFERING
	}
	
	fun setEventGuid(eventGuID: String) {
		getEventDetail(eventGuID)
	}
	
	private fun getEventDetail(eventGuID: String) {
		_event.value = Result.Loading
		if (context.isNetworkAvailable()) {
			getRemoteDetail(eventGuID)
		} else {
			val eventCache = eventDb.getEvent(eventGuID)
			if (eventCache != null)
				setEvent(eventCache)
			else _event.value = Result.Error(NetworkNotConnection())
		}
	}
	
	private fun getRemoteDetail(eventGuID: String) {
		getEventUseCase.execute(object : DisposableSingleObserver<Event>() {
			override fun onSuccess(t: Event) {
				setEvent(t)
			}
			
			override fun onError(e: Throwable) {
				val eventCache = eventDb.getEvent(eventGuID)
				if (eventCache != null)
					setEvent(eventCache)
				else
					_event.value = Result.Error(e)
			}
		}, eventGuID)
	}
	
	private fun setEvent(event: Event) {
		this.eventResult = event
		this._event.value = Result.Success(event)
		setSpectrogramImage()
		this._eventState.value = EventState.NONE
		initPlayer(event.audioOpusUrl)
		getClassifiedCation()
	}
	
	private fun setSpectrogramImage() {
		_spectrogramImage.value = "https://assets.rfcx.org/audio/${eventResult?.audioId}.png?width=512&height=512" +
				"&inline=1"
	}
	
	
	fun replaySound() {
		if (exoPlayer.playbackState == Player.STATE_ENDED) {
			exoPlayer.seekTo(0)
			exoPlayer.playWhenReady = true
		} else {
			eventResult?.audioOpusUrl?.let { initPlayer(it) }
		}
	}
	
	fun seekPlayerTo(timeMs: Long) {
		if (exoPlayer.playbackState != Player.STATE_IDLE) {
			exoPlayer.seekTo(timeMs)
			exoPlayer.playWhenReady = true
		}
	}
	
	private fun initPlayer(audioUrl: String) {
		
		val descriptorFactory =
				DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getString(R.string.app_name)))
		
		val thisAudioUrl: String = if (Build.VERSION.SDK_INT < 21) {
			audioUrl.replace("https://assets.rfcx.org/", "http://api-insecure.rfcx.org/v1/assets/")
		} else {
			audioUrl
		}
		
		val audioFile = File(context.cacheDir, "${eventResult?.audioId}.opus")
		val mediaSource = if (audioFile.exists()) {
			ExtractorMediaSource.Factory(descriptorFactory).createMediaSource(Uri.fromFile(audioFile))
		} else {
			ExtractorMediaSource.Factory(descriptorFactory).createMediaSource(Uri.parse(thisAudioUrl))
		}
		
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
					playerTimeHandler.removeCallbacks(playerTimeRunnable)
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
	
	private fun getClassifiedCation() {
		if (eventResult?.windows != null && eventResult?.windows?.isNotEmpty() == true) {
			// if even has windows no need to call api
			val eventWindows = eventResult?.windows?.filter {
				it.confidence > confidenceValue
			}
			
			val confidence = eventWindows?.map {
				Confidence(it.start.toLong(), it.confidence,
						it.end.toLong())
			}
			
			_classifiedCation.value = Result.Success(confidence ?: listOf())
			
			return
		}
	}
	
	fun reviewEvent(confirm: Boolean) {
		_reviewEvent.value = Result.Loading
		eventResult?.let {
			val requests = ReviewEventFactory(it.id, if (confirm) ReviewEventFactory.confirmEvent else ReviewEventFactory.rejectEvent)
			reviewEventUseCase.execute(object : DisposableSingleObserver<Unit>() {
				override fun onSuccess(t: Unit) {
					requestUpdateEvent(requests)
				}
				
				override fun onError(e: Throwable) {
					e.printStackTrace()
					// save to review unsent
					eventDb.save(EventReview(requests.eventGuID, requests.reviewConfirm,
							EventReview.UNSENT))
					val event = eventResult!!.apply {
						it.firstNameReviewer = context.getNameEmail() // update reviewer on offline
					}
					eventDb.saveEvent(event)
					_reviewEvent.value = Result.Success(Pair(event, requests))
					_eventState.value = EventState.REVIEWED	// invoke state to review
				}
				
			}, requests)
			
			ReviewEventSyncWorker.enqueue()
			
		} ?: run {
			_reviewEvent.value = Result.Error(IllegalStateException("Event is null."))
		}
	}
	
	fun requestUpdateEvent(requests: ReviewEventFactory) {
		if (!context.isNetworkAvailable()) {
			_reviewEvent.value = Result.Success(Pair(eventResult!!, requests))
		}
		
		getEventUseCase.execute(object : DisposableSingleObserver<Event>() {
			override fun onSuccess(event: Event) {
				_reviewEvent.value = Result.Success(Pair(event, requests))
				// invoke state to review
				_eventState.value = EventState.REVIEWED
			}
			
			override fun onError(e: Throwable) {
				e.printStackTrace()
				
				_event.value = Result.Error(e)
				_reviewEvent.value = Result.Success(Pair(eventResult!!, requests))
			}
		}, requests.eventGuID)
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
		const val confidenceValue = 0.8
	}
}

enum class EventState {
	NONE, REVIEWED
}