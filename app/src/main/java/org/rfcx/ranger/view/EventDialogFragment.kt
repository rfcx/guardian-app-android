package org.rfcx.ranger.view

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.crashlytics.android.Crashlytics
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_dialog_alert_event.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.GlideApp
import org.rfcx.ranger.util.getIconRes

class EventDialogFragment : DialogFragment(), OnMapReadyCallback {
	private var event: Event? = null
	private var onAlertConfirmCallback: OnAlertConfirmCallback? = null
	private val exoPlayer by lazy { ExoPlayerFactory.newSimpleInstance(this.context) }
	private val playerTimeHandler: Handler = Handler()
	private val delayTime = 100L
	private val maxProgress = 100_000
	
	override fun onAttach(context: Context?) {
		super.onAttach(context)
		if (context is OnAlertConfirmCallback) {
			onAlertConfirmCallback = context
		}
	}
	
	private val playerTimeRunnable = object : Runnable {
		override fun run() {
			// TODO update Progress
			updateSoundProgress()
			playerTimeHandler.postDelayed(this, delayTime)
		}
		
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		getBundle()
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
		return inflater.inflate(R.layout.fragment_dialog_alert_event, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initView()
		initPlayer()
		setupMap()
		
		replayButton.setOnClickListener {
			rePlay()
		}
		
		okButton.setOnClickListener { report(true) }
		cancelButton.setOnClickListener { report(false) }
	}
	
	override fun onResume() {
		super.onResume()
		// setup size of dialog
		val width = resources.getDimensionPixelSize(R.dimen.dialog_min_width)
		val height = resources.getDimensionPixelSize(R.dimen.dialog_height)
		dialog.window?.setLayout(width, height)
	}
	
	override fun onDestroyView() {
		val mapFragment = childFragmentManager
				.findFragmentByTag(MAP_TAG)
		mapFragment?.let {
			childFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
		}
		super.onDestroyView()
	}
	
	override fun onDestroy() {
		super.onDestroy()
		playerTimeHandler.removeCallbacks(playerTimeRunnable)
		try {
			exoPlayer.removeListener(exoPlayerListener)
			exoPlayer.release()
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
	
	override fun onDetach() {
		super.onDetach()
		if (onAlertConfirmCallback != null)
			onAlertConfirmCallback = null
	}
	
	private fun getBundle() {
		event = arguments?.getParcelable(keyEventArgs)
		if (event == null) {
			dismissAllowingStateLoss()
		}
	}
	
	@SuppressLint("SetTextI18n")
	private fun initView() {
		soundProgressSeekBar.isEnabled = false
		soundProgressSeekBar.max = maxProgress
		event?.let {
			eventTypeImageView.setImageResource(it.getIconRes())
			it.value?.let { value ->
				if (value.isEmpty()) return
				eventNameTextView.text = "${value.substring(0, 1).toUpperCase() + value.substring(1)}?"
			}
			
			// TODO FIX val of offset and duration
			it.audioGUID?.let { audioGuID ->
				GlideApp.with(spectrogramImageView)
						.load(getSpectrogramImageUrl(audioGuID, 0, 90L * 1000))
						.into(spectrogramImageView)
			}
			
		}
	}
	
	private fun rePlay() {
		Log.d("rePlay", "${exoPlayer.playbackState}")
		if (exoPlayer.playbackState == Player.STATE_ENDED) {
			exoPlayer.seekTo(0)
			exoPlayer.playWhenReady = true
		} else {
			initPlayer()
		}
	}
	
	private fun report(isCurrentAlert: Boolean) {
		if (isCurrentAlert) {
			onAlertConfirmCallback?.onCurrentAlert(event!!)
		} else {
			onAlertConfirmCallback?.onIncorrectAlert(event!!)
		}
		dismissAllowingStateLoss()
	}
	
	private fun initPlayer() {
		
		val opusSource = event?.audio?.opus
		if (!opusSource.isNullOrEmpty()) {
			val descriptorFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, getString(R.string.app_name)))
			
			val insecureMp3Source = opusSource.replace("https://assets.rfcx.org/", "http://api-insecure.rfcx.org/v1/assets/")
			val mediaSource = ExtractorMediaSource.Factory(descriptorFactory).createMediaSource(Uri.parse(insecureMp3Source))
			context?.let {
				exoPlayer.playWhenReady = true
				exoPlayer.prepare(mediaSource)
				exoPlayer.addListener(exoPlayerListener)
			}
		} else {
			loadingSoundProgressBar.visibility = View.INVISIBLE
		}
	}
	
	private fun setupMap() {
		var mapFragment: SupportMapFragment? = childFragmentManager.findFragmentByTag(MAP_TAG) as SupportMapFragment?
		if (mapFragment == null) {
			mapFragment = SupportMapFragment.newInstance()
			childFragmentManager.beginTransaction()
					.add(R.id.mapContainer, mapFragment, MAP_TAG)
					.commitNow()
			childFragmentManager.executePendingTransactions()
		}
		mapFragment?.getMapAsync(this)
	}
	
	private fun getSpectrogramImageUrl(audioGuId: String, offset: Long, duration: Long): String {
		return "https://assets.rfcx.org/audio/$audioGuId.png?width=512&height=256&offset=$offset&duration=$duration".also {
			Log.d("getSpectrogramImageUrl", it)
		}
		
	}
	
	override fun onMapReady(googleMap: GoogleMap?) {
		if (!isAdded) return
		googleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
		if (event?.latitude != null && event?.longitude != null) {
			googleMap?.addMarker(MarkerOptions()
					.position(LatLng(event!!.latitude!!, event!!.longitude!!))
					.title(event?.value)
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin)))
			googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
					LatLng(event!!.latitude!!, event!!.longitude!!), 15f))
			googleMap?.uiSettings?.isScrollGesturesEnabled = false
		}
	}
	
	private fun updateSoundProgress() {
		exoPlayer.let {
			val duration = it.duration
			val currentDuration = it.currentPosition
			val progress = maxProgress * currentDuration / duration
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				soundProgressSeekBar?.setProgress(progress.toInt(), true)
			} else {
				soundProgressSeekBar?.progress = progress.toInt()
			}
		}
	}
	
	private val exoPlayerListener = object : Player.EventListener {
		override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
			when (playbackState) {
				Player.STATE_BUFFERING -> {
					replayButton.visibility = View.INVISIBLE
					loadingSoundProgressBar.visibility = View.VISIBLE
				}
				Player.STATE_READY -> {
					replayButton.visibility = View.INVISIBLE
					loadingSoundProgressBar.visibility = View.INVISIBLE
					playerTimeHandler.postDelayed(playerTimeRunnable, delayTime)
				}
				Player.STATE_IDLE -> {
				
				}
				Player.STATE_ENDED -> {
					replayButton.visibility = View.VISIBLE
					playerTimeHandler.removeCallbacks(playerTimeRunnable)
				}
				
			}
			
		}
		
		override fun onPlayerError(error: ExoPlaybackException?) {
			Toast.makeText(context, R.string.can_not_play_audio, Toast.LENGTH_SHORT).show()
			loadingSoundProgressBar.visibility = View.INVISIBLE
			replayButton.visibility = View.VISIBLE
			Crashlytics.logException(error)
		}
	}
	
	companion object {
		private const val keyEventArgs = "AlertDialogFragment.Event"
		fun newInstance(event: Event): EventDialogFragment {
			val fragment = EventDialogFragment()
			val args = Bundle()
			args.putParcelable(keyEventArgs, event)
			fragment.arguments = args
			return fragment
		}
		
		private const val MAP_TAG = "MAP_FRAGMENT"
	}
	
	interface OnAlertConfirmCallback {
		fun onCurrentAlert(event: Event)
		fun onIncorrectAlert(event: Event)
	}
}