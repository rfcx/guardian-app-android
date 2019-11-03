package org.rfcx.ranger.view.report

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_report.*
import org.rfcx.ranger.R
import org.rfcx.ranger.data.local.WeeklySummaryData
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.service.AirplaneModeReceiver
import org.rfcx.ranger.service.ReportSyncWorker
import org.rfcx.ranger.util.*
import org.rfcx.ranger.widget.SoundRecordState
import org.rfcx.ranger.widget.WhatView
import org.rfcx.ranger.widget.WhenView
import java.io.File
import java.io.IOException
import java.util.*

class ReportActivity : BaseReportImageActivity(), OnMapReadyCallback {
	
	private var googleMap: GoogleMap? = null
	
	private var recordFile: File? = null
	private var recorder: MediaRecorder? = null
	private var player: MediaPlayer? = null
	private val locationPermissions by lazy { LocationPermissions(this) }
	private val recordPermissions by lazy { RecordingPermissions(this) }
	private var locationManager: LocationManager? = null
	private var lastLocation: Location? = null
	private val analytics by lazy { Analytics(this) }
	
	private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
		return ContextCompat.getDrawable(context, vectorResId)?.run {
			setBounds(0, 0, intrinsicWidth, intrinsicHeight)
			val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
			draw(Canvas(bitmap))
			BitmapDescriptorFactory.fromBitmap(bitmap)
		}
	}
	
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
		setContentView(R.layout.activity_report)
		
		bindActionbar()
		setupMap()
		setupWhatView()
		setupWhenView()
		setupRecordSoundProgressView()
		setupImageRecycler()
		
		reportButton.setOnClickListener {
			analytics.trackSubmitTheReportEvent()
			submitReport()
		}
	}
	
	override fun onResume() {
		registerReceiver(airplaneModeReceiver, IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED))
		super.onResume()
		analytics.trackScreen(Screen.ADDREPORT)
	}
	
	override fun onPause() {
		unregisterReceiver(airplaneModeReceiver)
		super.onPause()
	}
	
	override fun onDestroy() {
		super.onDestroy()
		locationManager?.removeUpdates(locationListener)
		val map = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
		map?.let {
			supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
		}
		stopPlaying()
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
	
	private fun setupMap() {
		val map = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
		map?.getMapAsync(this@ReportActivity)
	}
	
	override fun onMapReady(map: GoogleMap?) {
		googleMap = map
		googleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
		checkThenAccquireLocation()
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
		try {
			locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 1000L, 0f, locationListener)
			lastLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
			showLocationFinding()
			lastLocation?.let { markRangerLocation(it) }
		} catch (ex: SecurityException) {
			ex.printStackTrace()
		} catch (ex: IllegalArgumentException) {
			ex.printStackTrace()
		}
		
	}
	
	private fun markRangerLocation(location: Location) {
		lastLocation = location
		googleMap?.clear()
		val latLng = LatLng(location.latitude, location.longitude)
		googleMap?.addMarker(MarkerOptions()
				.position(latLng)
				.icon(bitmapDescriptorFromVector(this, R.drawable.ic_pin_map)))
		
		googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
				latLng, 15f))
		googleMap?.uiSettings?.isScrollGesturesEnabled = false
		locationStatusTextView.visibility = View.GONE
		validateForm()
	}
	
	private fun setupWhatView() {
		whatView.onWhatViewChangedListener = object : WhatView.OnWhatViewChangedListener {
			override fun onViewChange(event: String?) {
				validateForm()
			}
		}
	}
	
	private fun setupWhenView() {
		whenView.onWhenViewStateChangedListener = object : WhenView.OnWhenViewStateChangedListener {
			override fun onStateChange(state: Report.AgeEstimate) {
				validateForm()
			}
		}
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
		reportButton.isEnabled = eventSelected != null && whenState != Report.AgeEstimate.NONE && lastLocation != null
	}
	
	private fun submitReport() {
		val eventSelected = whatView.getEventSelected()
		val whenState = whenView.getState()
		if (eventSelected == null || whenState == Report.AgeEstimate.NONE) {
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