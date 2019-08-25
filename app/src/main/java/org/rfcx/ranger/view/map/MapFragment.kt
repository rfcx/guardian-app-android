package org.rfcx.ranger.view.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.service.LocationTrackerService
import org.rfcx.ranger.util.DateHelper
import org.rfcx.ranger.view.MainActivityEventListener
import org.rfcx.ranger.view.base.BaseFragment

class MapFragment : BaseFragment(), OnMapReadyCallback {
	
	private val mapViewModel: MapViewModel by viewModel()
	private var checkInPolyline: Polyline? = null
	private var checkInMarkers = arrayListOf<Marker>()
	private var retortMarkers = arrayListOf<Marker>()
	private var googleMap: GoogleMap? = null
	
	private var onCompletionCallback: ((Boolean) -> Unit)? = null
	
	private var locationManager: LocationManager? = null
	private var lastLocation: Location? = null
	
	private val locationListener = object : android.location.LocationListener {
		override fun onLocationChanged(p0: Location?) {
			p0?.let {
				moveCameraToCurrentLocation(it)
			}
		}
		
		override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
		override fun onProviderEnabled(p0: String?) {}
		override fun onProviderDisabled(p0: String?) {}
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_map, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupMap()
	}
	
	override fun onDestroyView() {
		val map = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
		map?.let {
			childFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
		}
		super.onDestroyView()
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == REQUEST_CHECK_LOCATION_SETTINGS) {
			onCompletionCallback?.invoke(resultCode == Activity.RESULT_OK)
		}
	}
	
	private fun setupMap() {
		val map = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
		map?.getMapAsync(this)
	}
	
	override fun onMapReady(map: GoogleMap?) {
		googleMap = map
		map?.let {
			it.mapType = GoogleMap.MAP_TYPE_SATELLITE
			displayReport(it)
			displayCheckIn(it)
			getLocation()
			map.setOnMapClickListener {
				(activity as MainActivityEventListener).hideBottomSheet()
			}
		}
		
		val permissionState = context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) }
		if (permissionState == PackageManager.PERMISSION_GRANTED) {
			Log.d("permissionState", "true")
			check { isAllowed: Boolean ->
				map?.isMyLocationEnabled = isAllowed
			}
		} else {
			Log.d("permissionState", "false")
		}
	}
	
	@SuppressLint("MissingPermission")
	private fun getLocation() {
		locationManager?.removeUpdates(locationListener)
		locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
		try {
//			locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 1000L, 0f, locationListener)
//			locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5 * 1000L, 0f, locationListener)
			lastLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
			lastLocation?.let { moveCameraToCurrentLocation(it) }
		} catch (ex: SecurityException) {
			ex.printStackTrace()
		} catch (ex: IllegalArgumentException) {
			ex.printStackTrace()
		}
	}
	
	private fun moveCameraToCurrentLocation(location: Location) {
		lastLocation = location
		googleMap?.clear()
		val latLng = LatLng(location.latitude, location.longitude)
		googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
				latLng, 15f))
	}
	
	private fun allowed(): Boolean {
		val permissionState = context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) }
		return permissionState == PackageManager.PERMISSION_GRANTED
	}
	
	private fun check(onCompletionCallback: (Boolean) -> Unit) {
		this.onCompletionCallback = onCompletionCallback
		if (allowed()) {
			verifySettings()
		}
	}
	
	private fun verifySettings() {
		val builder = LocationSettingsRequest.Builder().addLocationRequest(LocationTrackerService.locationRequest)
		context?.let {
			val client: SettingsClient = LocationServices.getSettingsClient(it)
			val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
			
			task.addOnSuccessListener {
				onCompletionCallback?.invoke(true)
			}
			
			task.addOnFailureListener { exception ->
				if (exception is ResolvableApiException) {
					// Location settings are not satisfied, but this can be fixed by showing the user a dialog
					try {
						// Show the dialog and check the result in onActivityResult()
						this.startIntentSenderForResult(exception.resolution.intentSender, REQUEST_CHECK_LOCATION_SETTINGS, null, 0, 0, 0, null)
					} catch (sendEx: IntentSender.SendIntentException) {
						// Ignore the error.
						sendEx.printStackTrace()
						onCompletionCallback?.invoke(false)
					}
				} else {
					onCompletionCallback?.invoke(false)
				}
			}
		}
	}
	
	private fun displayReport(map: GoogleMap) {
		
		map.setOnMarkerClickListener { marker ->
			
			Log.d(tag, "Map click $marker")
			if (marker.tag is Report) {
				val report = marker.tag as Report
				(activity as MainActivityEventListener)
						.showBottomSheet(MapDetailBottomSheetFragment.newInstance(report.id))
			} else {
				(activity as MainActivityEventListener).hideBottomSheet()
			}
			false
		}
		
		mapViewModel.getReports().observe(this, Observer { reports ->
			
			if (!isAdded || isDetached) return@Observer
			
			Log.d(tag, "${reports.count()}")
			
			retortMarkers.forEach {
				it.remove()
			}
			retortMarkers.clear()
			
			for (report in reports) {
				val latLng = LatLng(report.latitude, report.longitude)
				val marker = map.addMarker(MarkerOptions()
						.position(latLng)
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_report_pin_on_map)))
				marker.tag = report
				marker.zIndex = 1f
				retortMarkers.add(marker)
			}
			
			if (reports.isNotEmpty()) {
				val lastCheckIn = reports.last()
				moveMapTo(map, LatLng(lastCheckIn.latitude, lastCheckIn.longitude))
			}
		})
	}
	
	private fun displayCheckIn(map: GoogleMap) {
		// clear old markers
		checkInMarkers.forEach {
			it.remove()
		}
		checkInPolyline?.remove()
		checkInMarkers.clear()
		
		mapViewModel.getCheckIns().observe(this, Observer { checkIns ->
			
			if (!isAdded || isDetached) return@Observer
			
			Log.d(tag, "${checkIns.count()}")
			
			val polylineOptions = PolylineOptions()
			context?.let {
				val color = ContextCompat.getColor(it, R.color.check_in_polyline)
				polylineOptions.color(color)
			}
			
			for (checkIn in checkIns) {
				val latLng = LatLng(checkIn.latitude, checkIn.longitude)
				polylineOptions.add(latLng)
				checkInMarkers.add(map.addMarker(MarkerOptions()
						.position(latLng)
						.anchor(0.5f, 0.5f)
						.title(DateHelper.parse(checkIn.time))
						.snippet("${checkIn.latitude},${checkIn.latitude}")
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_chek_in_pin_on_map))))
				checkInPolyline = map.addPolyline(polylineOptions)
			}
			
			if (checkIns.isNotEmpty()) {
				val lastCheckIn = checkIns.last()
				moveMapTo(map, LatLng(lastCheckIn.latitude, lastCheckIn.longitude))
			}
		})
	}
	
	private fun moveMapTo(googleMap: GoogleMap, latLng: LatLng) {
		Log.d(tag, "moveMapTo $latLng")
		if (!isAdded || isDetached) return
		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
				LatLng(latLng.latitude, latLng.longitude), 18f))
	}
	
	companion object {
		fun newInstance(): MapFragment {
			return MapFragment()
		}
		
		const val REQUEST_CHECK_LOCATION_SETTINGS = 36
		const val tag = "MapFragment"
	}
}