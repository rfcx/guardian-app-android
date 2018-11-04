package org.rfcx.ranger.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_report.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.report.ReportTypeAdapter
import org.rfcx.ranger.util.isLocationAllow

class ReportActivity : AppCompatActivity(), OnMapReadyCallback {
	
	private var googleMap: GoogleMap? = null
	private val intervalLocationUpdate: Long = 30 * 1000 // 30 seconds
	private var fusedLocationClient: FusedLocationProviderClient? = null
	
	private var locationCallback: LocationCallback = object : LocationCallback() {
		override fun onLocationResult(locationResult: LocationResult?) {
			super.onLocationResult(locationResult)
			locationResult?.lastLocation?.let {
				markRangerLocation(it)
			}
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_report)
		
		bindActionbar()
		setupMap()
		setupReportWhatAdapter()
	}
	
	override fun onPause() {
		super.onPause()
		fusedLocationClient?.removeLocationUpdates(locationCallback)
	}
	
	override fun onResume() {
		super.onResume()
		if (!isLocationAllow()) {
			requestPermissions()
		} else {
			getLocation()
		}
	}
	
	override fun onDestroy() {
		super.onDestroy()
		val map = supportFragmentManager?.findFragmentById(R.id.mapView) as SupportMapFragment?
		map?.let {
			supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
		}
		
	}
	
	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			android.R.id.home -> finish()
		}
		return super.onOptionsItemSelected(item)
	}
	
	override fun onMapReady(p0: GoogleMap?) {
		googleMap = p0
		if (isLocationAllow()) getLocation()
		
	}
	
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
	                                        grantResults: IntArray) {
		if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// start location service
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
		val locationRequest = LocationRequest()
		locationRequest.interval = intervalLocationUpdate
		locationRequest.priority = LocationRequest.PRIORITY_LOW_POWER
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
		fusedLocationClient?.lastLocation?.addOnSuccessListener {
			if (it != null) {
				markRangerLocation(it)
			}
		}
		fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null)
	}
	
	private fun markRangerLocation(location: Location) {
		googleMap?.clear()
		val latLng = LatLng(location.latitude, location.longitude)
		googleMap?.addMarker(MarkerOptions()
				.position(latLng)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin)))
		
		googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
				latLng, 15f))
		googleMap?.uiSettings?.isScrollGesturesEnabled = false
	}
	
	private fun requestPermissions() {
		val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this@ReportActivity,
				Manifest.permission.ACCESS_FINE_LOCATION)
		
		if (!shouldProvideRationale) {
			val dialogBuilder: AlertDialog.Builder =
					AlertDialog.Builder(this@ReportActivity).apply {
						setTitle(null)
						setMessage(R.string.location_permission_msg)
						setPositiveButton(R.string.go_to_setting) { _, _ ->
							val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
									Uri.parse("package:$packageName"))
							intent.addCategory(Intent.CATEGORY_DEFAULT)
							intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
							startActivity(intent)
						}
					}
			dialogBuilder.create().show()
			
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
						REQUEST_PERMISSIONS_REQUEST_CODE)
			}
		}
	}
	
	private fun setupReportWhatAdapter() {
		val layoutManager = GridLayoutManager(this@ReportActivity, 5)
		reportTypeRecycler.layoutManager = layoutManager
		reportTypeRecycler.adapter = ReportTypeAdapter()
	}
	
	
	companion object {
		private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
	}
}