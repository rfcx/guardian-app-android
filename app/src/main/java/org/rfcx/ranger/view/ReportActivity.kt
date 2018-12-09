package org.rfcx.ranger.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_report.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.OnMessageItemClickListener
import org.rfcx.ranger.adapter.report.ReportTypeAdapter
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.service.ReportSyncWorker
import org.rfcx.ranger.util.DateHelper
import org.rfcx.ranger.util.LocationPermissions
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.isRecordingAudioAllowed
import org.rfcx.ranger.widget.OnStatChangeListener
import org.rfcx.ranger.widget.SoundRecordState
import org.rfcx.ranger.widget.WhenView
import java.io.File
import java.io.IOException

class ReportActivity : AppCompatActivity(), OnMapReadyCallback {

	private var googleMap: GoogleMap? = null
	private val reportAdapter = ReportTypeAdapter()

	private var recordFile: File? = null
	private var recorder: MediaRecorder? = null
	private var player: MediaPlayer? = null
	private val locationPermissions by lazy { LocationPermissions(this) }
	private var locationManager: LocationManager? = null
	private var lastLocation: Location? = null
	private var fusedLocationClient: FusedLocationProviderClient? = null

	private var locationCallback: LocationCallback = object : LocationCallback() {
		override fun onLocationResult(locationResult: LocationResult?) {
			super.onLocationResult(locationResult)
			locationResult?.lastLocation?.let {
				markRangerLocation(it)
			}
		}
	}
	
	private val locationListener = object : android.location.LocationListener {
		override fun onLocationChanged(p0: Location?) {
			p0?.let {
				markRangerLocation(it)
			}
		}
		override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
		override fun onProviderEnabled(p0: String?) {}
		override fun onProviderDisabled(p0: String?) {}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_report)
		
		bindActionbar()
		setupMap()
		setupReportWhatAdapter()
		setupWhenView()
		
		reportButton.setOnClickListener {
			submitReport()
		}
		
		soundRecordProgressView.onStatChangeListener = object : OnStatChangeListener {
			override fun onStateChanged(state: SoundRecordState) {
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
	}
	
	override fun onPause() {
		super.onPause()
		fusedLocationClient?.removeLocationUpdates(locationCallback)
	}
	
	override fun onDestroy() {
		super.onDestroy()
		val map = supportFragmentManager?.findFragmentById(R.id.mapView) as SupportMapFragment?
		map?.let {
			supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		locationPermissions.handleActivityResult(requestCode, resultCode)
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		locationPermissions.handleRequestResult(requestCode, grantResults)

		if (requestCode == REQUEST_PERMISSIONS_RECORD_REQUEST_CODE) {
			if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
				val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this@ReportActivity,
						Manifest.permission.RECORD_AUDIO)

				if (!shouldProvideRationale) {
					val dialogBuilder: AlertDialog.Builder =
							AlertDialog.Builder(this@ReportActivity).apply {
								setTitle(null)
								setMessage(R.string.record_audio_permission_msg)
								setPositiveButton(R.string.go_to_setting) { _, _ ->
									val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
											Uri.parse("package:$packageName"))
									intent.addCategory(Intent.CATEGORY_DEFAULT)
									intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
									startActivity(intent)
								}
							}
					dialogBuilder.create().show()
				}
			}
		}
	}
	
	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			android.R.id.home -> finish()
		}
		return super.onOptionsItemSelected(item)
	}
	
	override fun onMapReady(map: GoogleMap?) {
		googleMap = map
		googleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
		locationPermissions.check { isAllowed: Boolean ->
			if (isAllowed) {
				getLocation()
			}
		}
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
		val map = supportFragmentManager?.findFragmentById(R.id.mapView) as SupportMapFragment?
		map?.getMapAsync(this@ReportActivity)
	}
	
	@SuppressLint("MissingPermission")
	private fun getLocation() {
		if (isDestroyed) return
		locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
		try {
			locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 1000L, 0f, locationListener)
			locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5 * 1000L, 0f, locationListener)
			lastLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
			lastLocation?.let { markRangerLocation(it) }
		} catch (ex: java.lang.SecurityException) {
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
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin)))
		
		googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
				latLng, 15f))
		googleMap?.uiSettings?.isScrollGesturesEnabled = false
		validateForm()
	}
	
	private fun setupReportWhatAdapter() {
		val layoutManager = GridLayoutManager(this@ReportActivity, 5)
		reportTypeRecycler.layoutManager = layoutManager
		reportTypeRecycler.adapter = reportAdapter
		reportAdapter.onMessageItemClickListener = object : OnMessageItemClickListener {
			override fun onMessageItemClick(position: Int) {
				validateForm()
			}
		}
	}
	
	private fun setupWhenView() {
		whenView.onWhenViewStatChangedListener = object : WhenView.OnWhenViewStatChangedListener {
			override fun onStateChange(state: WhenView.State) {
				validateForm()
			}
		}
		whenView.setState(WhenView.State.NOW)
	}
	
	private fun validateForm() {
		val reportTypeItem = reportAdapter.getSelectedItem()
		val whenState = whenView.getState()
		reportButton.isEnabled = reportTypeItem != null && whenState != WhenView.State.NONE && lastLocation != null
	}
	
	private fun submitReport() {
		val reportTypeItem = reportAdapter.getSelectedItem()
		val whenState = whenView.getState()
		if (reportTypeItem == null || whenState == WhenView.State.NONE) {
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
		
		val site = Preferences.getInstance(this).getString(Preferences.DEFAULT_SITE, "")
		val time = DateHelper.getIsoTime()
		val lat = lastLocation?.latitude ?: 0.0
		val lon = lastLocation?.longitude ?: 0.0
		
		val report = Report(value = reportTypeItem.type, site = site, reportedAt = time, latitude = lat, longitude = lon, ageEstimate = whenState.ageEstimate, audioLocation = recordFile?.canonicalPath)
		
		ReportDb().save(report)
		ReportSyncWorker.enqueue()
		
		finish()
	}
	
	private fun record() {
		if (!isRecordingAudioAllowed()) {
			soundRecordProgressView.state = SoundRecordState.NONE
			requestRecordAudioPermission()
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
	
	private fun requestRecordAudioPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),
					REQUEST_PERMISSIONS_RECORD_REQUEST_CODE)
		}
	}
	
	companion object {
		private const val REQUEST_PERMISSIONS_RECORD_REQUEST_CODE = 36
	}
}