package org.rfcx.ranger.view.report

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.utils.BitmapUtils
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_report.*
import org.rfcx.ranger.R
import org.rfcx.ranger.data.local.WeeklySummaryData
import org.rfcx.ranger.databinding.ActivityReportBinding
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.service.AirplaneModeReceiver
import org.rfcx.ranger.service.ReportSyncWorker
import org.rfcx.ranger.util.*
import org.rfcx.ranger.view.map.MapFragment.Companion.MAPBOX_ACCESS_TOKEN
import org.rfcx.ranger.widget.SoundRecordState
import org.rfcx.ranger.widget.WhatView
import org.rfcx.ranger.widget.WhenView
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.concurrent.timerTask


class ReportActivity : BaseReportImageActivity(), OnMapReadyCallback {
	
	private var recordFile: File? = null
	private var recorder: MediaRecorder? = null
	private var player: MediaPlayer? = null
	private val locationPermissions by lazy { LocationPermissions(this) }
	private val recordPermissions by lazy { RecordingPermissions(this) }
	private var locationManager: LocationManager? = null
	private var lastLocation: Location? = null
	private val analytics by lazy { Analytics(this) }
	
	private val waitingForLocationTimer: Timer = Timer()
	
	private lateinit var binding: ActivityReportBinding
	private lateinit var mapView: MapView
	private lateinit var mapBoxMap: MapboxMap
	
	private val locationListener = object : android.location.LocationListener {
		override fun onLocationChanged(p0: Location?) {
			p0?.let {
				markRangerLocation(it)
			}
		}
		
		override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
		override fun onProviderEnabled(p0: String?) {
			showLocationFinding()
		}
		
		override fun onProviderDisabled(p0: String?) {
			showLocationMessageError(getString(R.string.notification_location_not_availability))
		}
	}
	
	private val onAirplaneModeCallback: (Boolean) -> Unit = { isOnAirplaneMode ->
		if (isOnAirplaneMode && !isDestroyed) {
			showLocationMessageError("${getString(R.string.in_air_plane_mode)} \n ${getString(R.string.pls_off_air_plane_mode)}")
		} else {
			checkThenAccquireLocation()
		}
	}
	
	private val airplaneModeReceiver = AirplaneModeReceiver(onAirplaneModeCallback)
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		Mapbox.getInstance(this, MAPBOX_ACCESS_TOKEN)
		binding = DataBindingUtil.setContentView(this, R.layout.activity_report)
		
		bindActionbar()
		setupMap(savedInstanceState)
		setupRecordSoundProgressView()
		setupImageRecycler()
		setupOnClick()
	}
	
	override fun onResume() {
		registerReceiver(airplaneModeReceiver, IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED))
		super.onResume()
		mapView.onResume()
		analytics.trackScreen(Screen.ADDREPORT)
	}
	
	override fun onPause() {
		unregisterReceiver(airplaneModeReceiver)
		super.onPause()
		mapView.onPause()
	}
	
	override fun onDestroy() {
		super.onDestroy()
		locationManager?.removeUpdates(locationListener)
		mapView.onDestroy()
		stopPlaying()
		waitingForLocationTimer.cancel()
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		
		locationPermissions.handleActivityResult(requestCode, resultCode)
	}
	
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		
		locationPermissions.handleRequestResult(requestCode, grantResults)
		recordPermissions.handleRequestResult(requestCode, grantResults)
	}
	
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			android.R.id.home -> finish()
		}
		return super.onOptionsItemSelected(item)
	}
	
	private fun bindActionbar() {
		setSupportActionBar(toolbar)
		supportActionBar?.apply {
			setDisplayHomeAsUpEnabled(true)
			setDisplayShowHomeEnabled(true)
			elevation = 0f
			title = getString(R.string.report_title)
		}
	}
	
	private fun setupOnClick() {
		binding.onClickReportButton = View.OnClickListener {
			analytics.trackSubmitTheReportEvent()
			submitReport()
		}
		
		binding.onWhatViewChangedListener = object : WhatView.OnWhatViewChangedListener {
			override fun onViewChange(event: String?) {
				validateForm()
			}
		}
		
		binding.onWhenViewStateChangedListener = object : WhenView.OnWhenViewStateChangedListener {
			override fun onStateChange(state: Report.AgeEstimate) {
				validateForm()
			}
		}
		
		binding.onClickCheckSubmit = View.OnClickListener {
			showInputRequired()
		}
		
		binding.canSubmitReport = false // default
	}
	
	private fun setupMap(savedInstanceState: Bundle?) {
		mapView = findViewById(R.id.mapBoxView)
		mapView.onCreate(savedInstanceState)
		mapView.getMapAsync(this)
	}
	
	override fun onMapReady(mapboxMap: MapboxMap) {
		mapBoxMap = mapboxMap
		mapboxMap.setStyle(Style.OUTDOORS) {
			checkThenAccquireLocation()
		}
	}
	
	private fun checkThenAccquireLocation() {
		if (isDestroyed) return
		if (isOnAirplaneMode()) {
			showLocationMessageError("${getString(R.string.in_air_plane_mode)} \n ${getString(R.string.pls_off_air_plane_mode)}")
		} else {
			locationPermissions.check { isAllowed: Boolean ->
				if (isAllowed) {
					getLocation()
				} else {
					showLocationMessageError(getString(R.string.notification_location_not_availability))
				}
			}
		}
	}
	
	@SuppressLint("MissingPermission")
	private fun getLocation() {
		if (isDestroyed) return
		locationManager?.removeUpdates(locationListener)
		locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
		waitingForLocationTimer.schedule(timerTask {
			if (lastLocation == null) {
				runOnUiThread {
					showLocationMessageError(getString(R.string.in_air_plane_mode))
					AlertDialog.Builder(this@ReportActivity)
							.setTitle(null)
							.setMessage("${getString(R.string.in_air_plane_mode)}. ${getString(R.string.please_try_again_later)}")
							.setCancelable(true)
							.setPositiveButton(R.string.button_ok) { dialog, _ ->
								dialog.dismiss()
								finish()
							}.show()
				}
			}
		}, 30 * 1000) // waiting for 30 sec if cannot get location show error
		try {
			locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 1000L, 0f, locationListener)
			lastLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
			showLocationFinding()
			lastLocation?.let { markRangerLocation(it) }
		} catch (ex: SecurityException) {
			showLocationMessageError(getString(R.string.in_air_plane_mode))
			ex.printStackTrace()
		} catch (ex: IllegalArgumentException) {
			showLocationMessageError(getString(R.string.in_air_plane_mode))
			ex.printStackTrace()
		}
		
	}
	
	private fun markRangerLocation(location: Location) {
		lastLocation = location
		mapBoxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15.0))
		
		val symbolManager = mapBoxMap.style?.let { SymbolManager(mapView, mapBoxMap, it) }
		symbolManager?.iconAllowOverlap = true
		symbolManager?.iconIgnorePlacement = true
		
		val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_pin_map, null)
		val mBitmap = BitmapUtils.getBitmapFromDrawable(drawable)
		if (mBitmap != null) {
			mapBoxMap.style?.addImage("pin-map", mBitmap)
		}
		
		symbolManager?.create(SymbolOptions()
				.withLatLng(LatLng(location.latitude, location.longitude))
				.withIconImage("pin-map")
				.withIconSize(1.0f))
		
		locationStatusTextView.visibility = View.GONE
		validateForm()
	}
	
	private fun setupRecordSoundProgressView() {
		soundRecordProgressView.onStateChangeListener = { state ->
			when (state) {
				SoundRecordState.NONE -> {
					recordFile?.deleteOnExit()
					recordFile = null
				}
				SoundRecordState.RECORDING -> {
					record()
				}
				SoundRecordState.STOPPED_RECORD -> {
					stopRecording()
				}
				SoundRecordState.PLAYING -> {
					startPlaying()
				}
				SoundRecordState.STOP_PLAYING -> {
					stopPlaying()
				}
			}
		}
	}
	
	private fun validateForm() {
		val eventSelected = whatView.getEventSelected()
		val whenState = whenView.getState()
		val enable = eventSelected != null && whenState != Report.AgeEstimate.NONE && lastLocation != null
		
		// validate input requited
		if (eventSelected != null) {
			binding.warningWhatRequired = false
		}
		
		if (whenState != Report.AgeEstimate.NONE) {
			binding.warningWhenRequired = false
		}
		
		reportButton.isEnabled = enable
		binding.canSubmitReport = enable
	}
	
	private fun showInputRequired() {
		val eventSelected = whatView.getEventSelected()
		val whenState = whenView.getState()
		binding.warningWhatRequired = (eventSelected == null)
		binding.warningWhenRequired = (whenState == Report.AgeEstimate.NONE)
		if (eventSelected == null || whenState == Report.AgeEstimate.NONE) {
			scrollView.fullScroll(ScrollView.FOCUS_UP)
		}
	}
	
	private fun submitReport() {
		val eventSelected = whatView.getEventSelected()
		val whenState = whenView.getState()
		if (eventSelected == null || whenState == Report.AgeEstimate.NONE) {
			Log.d("Report", "submit! but no successful!")
			validateForm()
			return
		}
		
		if (lastLocation == null) {
			if (!locationPermissions.allowed()) {
				locationPermissions.check { getLocation() }
			} else {
				Snackbar.make(rootView, R.string.report_location_null, Snackbar.LENGTH_LONG).show()
			}
			return
		}
		
		val site = getSiteName()
		val time = Date()
		val lat = lastLocation?.latitude ?: 0.0
		val lon = lastLocation?.longitude ?: 0.0
		val note: String? = if (noteEditText.text?.trim().toString().isEmpty()) {
			null
		} else noteEditText.text?.trim().toString()
		Log.d("getSiteName", getSiteName())
		
		val report = Report(value = eventSelected, site = site, reportedAt = time,
				latitude = lat, longitude = lon, notes = note, ageEstimateRaw = whenState.value,
				audioLocation = recordFile?.canonicalPath)
		
		ReportDb(Realm.getInstance(RealmHelper.migrationConfig())).save(report, reportImageAdapter.getNewAttachImage())
		WeeklySummaryData(Preferences(this)).adJustReportSubmitCount()
		ReportSyncWorker.enqueue()
		finish()
	}
	
	private fun record() {
		if (!recordPermissions.allowed()) {
			soundRecordProgressView.state = SoundRecordState.NONE
			recordPermissions.check { }
		} else {
			startRecord()
		}
	}
	
	private fun startRecord() {
		recordFile = File.createTempFile("Record${System.currentTimeMillis()}", ".mp3", this.cacheDir)
		recordFile?.let {
			recorder = MediaRecorder().apply {
				setAudioSource(MediaRecorder.AudioSource.MIC)
				setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
				setOutputFile(recordFile!!.absolutePath)
				setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
				try {
					prepare()
				} catch (e: IOException) {
					e.printStackTrace()
					soundRecordProgressView.state = SoundRecordState.NONE
					Snackbar.make(rootView, R.string.error_common, Snackbar.LENGTH_LONG).show()
				}
				start()
			}
		}
	}
	
	private fun stopRecording() {
		recorder?.apply {
			try {
				stop()
				release()
			} catch (e: Exception) {
				e.printStackTrace()
				soundRecordProgressView.state = SoundRecordState.NONE
				Snackbar.make(rootView, R.string.error_common, Snackbar.LENGTH_LONG).show()
			}
		}
		recorder = null
	}
	
	private fun startPlaying() {
		if (recordFile == null) {
			soundRecordProgressView.state = SoundRecordState.NONE
			return
		}
		player = MediaPlayer().apply {
			try {
				setDataSource(recordFile!!.absolutePath)
				prepare()
				start()
				setOnCompletionListener {
					soundRecordProgressView.state = SoundRecordState.STOP_PLAYING
				}
			} catch (e: IOException) {
				soundRecordProgressView.state = SoundRecordState.STOP_PLAYING
				Snackbar.make(rootView, R.string.error_common, Snackbar.LENGTH_LONG).show()
				e.printStackTrace()
			}
		}
	}
	
	private fun stopPlaying() {
		player?.release()
		player = null
	}
	
	private fun setupImageRecycler() {
		attachImageRecycler.apply {
			adapter = reportImageAdapter
			layoutManager = LinearLayoutManager(this@ReportActivity, LinearLayoutManager.HORIZONTAL, false)
			setHasFixedSize(true)
		}
		reportImageAdapter.setImages(arrayListOf())
	}
	
	override fun onStop() {
		super.onStop()
		mapView.onStop()
	}
	
	override fun onLowMemory() {
		super.onLowMemory()
		mapView.onLowMemory()
	}
	
	override fun onStart() {
		super.onStart()
		mapView.onStart()
	}
	
	override fun didAddImages(imagePaths: List<String>) {}
	override fun didRemoveImage(imagePath: String) {}
	
	companion object {
		fun startIntent(context: Context?) {
			context?.let {
				val intent = Intent(it, ReportActivity::class.java)
				it.startActivity(intent)
			}
		}
	}
	
	private fun showLocationMessageError(message: String) {
		locationStatusTextView.text = message
		locationStatusTextView.setBackgroundResource(R.color.location_status_failed_bg)
		locationStatusTextView.visibility = View.VISIBLE
	}
	
	private fun showLocationFinding() {
		locationStatusTextView.text = getString(R.string.notification_location_loading)
		locationStatusTextView.setBackgroundResource(R.color.location_status_loading_bg)
		locationStatusTextView.visibility = View.VISIBLE
	}
	
}