package org.rfcx.ranger.view

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
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

class EventDialogFragment : DialogFragment(), OnMapReadyCallback, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
	private var event: Event? = null
	private var mediaPlayer: MediaPlayer? = null
	private var onAlertConfirmCallback: OnAlertConfirmCallback? = null
	
	private val playerTimeHandler: Handler = Handler()
	private val delayTime = 100L
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
			if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
				mediaPlayer?.stop()
			}
			mediaPlayer?.release()
			mediaPlayer = null
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
		event?.let {
			eventTypeImageView.setImageResource(it.getIconRes())
			it.value?.let { value ->
				if (value.isEmpty()) return
				eventNameTextView.text = "${value.substring(0, 1).toUpperCase() + value.substring(1)}?"
			}
			
			// TODO FIX val of offset and duration
			it.audioGUID?.let { audioGuID ->
				GlideApp.with(spectrogramImageView)
						.load(getSpectrogramImageUrl(audioGuID, 0, 30000))
						.into(spectrogramImageView)
			}
			
		}
	}
	
	private fun rePlay() {
		try {
			mediaPlayer?.start().also {
				playerTimeHandler.postDelayed(playerTimeRunnable, delayTime)
			}
			replayButton.visibility = View.INVISIBLE
		} catch (e: Exception) {
			e.printStackTrace()
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
		mediaPlayer = MediaPlayer()
		mediaPlayer?.setWakeMode(context,
				PowerManager.PARTIAL_WAKE_LOCK)
		mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
		mediaPlayer?.setOnPreparedListener(this@EventDialogFragment)
		mediaPlayer?.setOnCompletionListener(this@EventDialogFragment)
		mediaPlayer?.setOnErrorListener(this@EventDialogFragment)
		
		val mp3Source = event?.audio?.mp3
		if (!mp3Source.isNullOrEmpty()) {
			val insecureMp3Source = mp3Source.replace("https://assets.rfcx.org/", "http://api-insecure.rfcx.org/v1/assets/")
			context?.let {
				mediaPlayer?.setDataSource(it, Uri.parse(insecureMp3Source))
				mediaPlayer?.prepareAsync()
			}
		} else {
			loadingSoundProgressBar.visibility = View.INVISIBLE
		}
	}
	
	override fun onError(player: MediaPlayer?, p1: Int, p2: Int): Boolean {
		// TODO report to error
		loadingSoundProgressBar.visibility = View.INVISIBLE
		spectrogramImageView.visibility = View.INVISIBLE
		return false
	}
	
	override fun onCompletion(player: MediaPlayer?) {
		replayButton.visibility = View.VISIBLE
		playerTimeHandler.removeCallbacks(playerTimeRunnable)
	}
	
	override fun onPrepared(player: MediaPlayer?) {
		loadingSoundProgressBar.visibility = View.INVISIBLE
		mediaPlayer?.start().also {
			playerTimeHandler.postDelayed(playerTimeRunnable, delayTime)
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
		return "https://assets.rfcx.org/audio/$audioGuId.png?width=512&height=256&offset=$offset&duration=$duration"
		
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
		mediaPlayer?.let {
			val duration = it.duration
			val currentDuration = it.currentPosition
			soundProgressSeekBar.max = duration
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				soundProgressSeekBar.setProgress(currentDuration, true)
			} else {
				soundProgressSeekBar.progress = currentDuration
			}
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