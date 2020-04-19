package org.rfcx.ranger.view.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.JsonPrimitive
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.utils.BitmapUtils
import kotlinx.android.synthetic.main.fragment_map.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.service.AirplaneModeReceiver
import org.rfcx.ranger.util.Analytics
import org.rfcx.ranger.util.LocationPermissions
import org.rfcx.ranger.util.Screen
import org.rfcx.ranger.util.isOnAirplaneMode
import org.rfcx.ranger.view.MainActivityEventListener
import org.rfcx.ranger.view.base.BaseFragment

class MapFragment : BaseFragment(), OnMapReadyCallback {
	
	private val mapViewModel: MapViewModel by viewModel()
	private var checkInPolyline: Polyline? = null
	private var checkInMarkers = arrayListOf<Marker>()
	private var retortMarkers = arrayListOf<Marker>()
	private val locationPermissions by lazy { activity?.let { LocationPermissions(it) } }
	private var locationManager: LocationManager? = null
	private var lastLocation: Location? = null
	private val analytics by lazy { context?.let { Analytics(it) } }
	
	private lateinit var mapView: MapView
	private lateinit var mapBoxMap: MapboxMap
	private var currentStyle: String = Style.OUTDOORS
	
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
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		context?.let { Mapbox.getInstance(it, MAPBOX_ACCESS_TOKEN) }
	}
	
	private val onAirplaneModeCallback: (Boolean) -> Unit = { isOnAirplaneMode ->
		if (isOnAirplaneMode) {
			showLocationMessageError("${getString(R.string.in_air_plane_mode)} \n ${getString(R.string.pls_off_air_plane_mode)}")
		} else {
			checkThenAccquireLocation()
		}
	}
	
	private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
		return ContextCompat.getDrawable(context, vectorResId)?.run {
			setBounds(0, 0, intrinsicWidth, intrinsicHeight)
			val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
			draw(Canvas(bitmap))
			BitmapDescriptorFactory.fromBitmap(bitmap)
		}
	}
	
	private val airplaneModeReceiver = AirplaneModeReceiver(onAirplaneModeCallback)
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_map, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		mapView = view.findViewById(R.id.mapView)
		mapView.onCreate(savedInstanceState)
		mapView.getMapAsync(this)
	}
	
	override fun onDestroyView() {
		val map = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
		map?.let {
			childFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
		}
		super.onDestroyView()
	}
	
	override fun onResume() {
		activity?.registerReceiver(airplaneModeReceiver, IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED))
		super.onResume()
		mapView.onResume()
	}
	
	override fun onStart() {
		super.onStart()
		mapView.onStart()
	}
	
	override fun onStop() {
		super.onStop()
		mapView.onStop()
	}
	
	override fun onLowMemory() {
		super.onLowMemory()
		mapView.onLowMemory()
	}
	
	override fun onHiddenChanged(hidden: Boolean) {
		super.onHiddenChanged(hidden)
		if (!hidden) {
			analytics?.trackScreen(Screen.MAP)
			checkThenAccquireLocation()
		}
	}
	
	override fun onPause() {
		activity?.unregisterReceiver(airplaneModeReceiver)
		super.onPause()
		mapView.onPause()
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		locationPermissions?.handleActivityResult(requestCode, resultCode)
	}
	
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		locationPermissions?.handleRequestResult(requestCode, grantResults)
	}
	
	override fun onMapReady(mapboxMap: MapboxMap) {
		mapBoxMap = mapboxMap
		mapboxMap.setStyle(Style.OUTDOORS) {
			mapboxMap.setMinZoomPreference(10.0)
			getCurrentLocation(mapboxMap)
			switchMap(mapboxMap)
			setDisplay()
			checkThenAccquireLocation()
		}
	}
	
	private fun switchMap(mapboxMap: MapboxMap) {
		switchButton.setOnClickListener {
			currentStyle = if (currentStyle == Style.OUTDOORS) {
				mapboxMap.setStyle(Style.SATELLITE)
				Style.SATELLITE
			} else {
				mapboxMap.setStyle(Style.OUTDOORS)
				Style.OUTDOORS
			}
		}
	}
	
	private fun getCurrentLocation(mapboxMap: MapboxMap) {
		context?.let {
			val customLocationComponentOptions = LocationComponentOptions.builder(it)
					.trackingGesturesManagement(true)
					.accuracyColor(ContextCompat.getColor(it, R.color.colorPrimary))
					.build()
			
			val locationComponentActivationOptions = mapboxMap.style?.let { style ->
				LocationComponentActivationOptions.builder(it, style)
						.locationComponentOptions(customLocationComponentOptions)
						.build()
			}
			
			mapboxMap.locationComponent.apply {
				if (locationComponentActivationOptions != null) {
					activateLocationComponent(locationComponentActivationOptions)
				}
				
				isLocationComponentEnabled = true
				cameraMode = CameraMode.TRACKING
				renderMode = RenderMode.COMPASS
			}
			
		}
	}
	
	private fun checkThenAccquireLocation() {
		if (context?.isOnAirplaneMode()!!) {
			showLocationMessageError("${getString(R.string.in_air_plane_mode)} \n ${getString(R.string.pls_off_air_plane_mode)}")
		} else {
			locationPermissions?.check { isAllowed: Boolean ->
				if (isAllowed) {
					getLocation()
				} else {
					setDisplay()
				}
			}
		}
	}
	
	@SuppressLint("MissingPermission")
	private fun getLocation() {
		if (!isAdded || isDetached) return
		layoutAlertAirplaneMode?.visibility = View.GONE
		
		locationManager?.removeUpdates(locationListener)
		locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
		try {
			lastLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
			lastLocation?.let { moveCameraToCurrentLocation(it) }
			setDisplay()
		} catch (ex: SecurityException) {
			ex.printStackTrace()
		} catch (ex: IllegalArgumentException) {
			ex.printStackTrace()
		}
	}
	
	private fun setDisplay() {
		displayReport()
//		googleMap?.let { displayCheckIn(it) }
//		googleMap?.setOnMapClickListener {
//			(activity as MainActivityEventListener).hideBottomSheet()
//		}
	}
	
	private fun displayReport() {
		val symbolManager = mapBoxMap.style?.let { SymbolManager(mapView, mapBoxMap, it) }
		symbolManager?.iconAllowOverlap = true
		symbolManager?.iconIgnorePlacement = true
		
		val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_pin_map, null)
		val mBitmap = BitmapUtils.getBitmapFromDrawable(drawable)
		if (mBitmap != null) {
			mapBoxMap.style?.addImage("pin-map", mBitmap)
		}
		
		mapViewModel.getReports().observe(this, Observer { reports ->
			if (!isAdded || isDetached) return@Observer
			for (report in reports) {
				symbolManager?.create(SymbolOptions()
						.withLatLng(LatLng(report.latitude, report.longitude))
						.withIconImage("pin-map")
						.withIconSize(1.0f)
						.withData(JsonPrimitive(report.id)))
			}
			if (reports.isNotEmpty()) {
				val lastCheckIn = reports.last()
				moveMapTo(LatLng(lastCheckIn.latitude, lastCheckIn.longitude))
			}
		})
		
		symbolManager?.addClickListener { symbol ->
			(activity as MainActivityEventListener).showBottomSheet(ReportViewPagerFragment.newInstance(symbol.data.toString().toInt()))
		}
	}
	
	private fun moveCameraToCurrentLocation(location: Location) {
		lastLocation = location
		val latLng = LatLng(location.latitude, location.longitude)
		mapBoxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.0))
	}
	
//	private fun displayReport(map: GoogleMap) {
//		map.setOnMarkerClickListener { marker ->
//			Log.d(tag, "Map click $marker")
//			if (marker.tag is Report) {
//				val report = marker.tag as Report
//				(activity as MainActivityEventListener)
//						.showBottomSheet(ReportViewPagerFragment.newInstance(report.id))
//				return@setOnMarkerClickListener true
//			} else {
//				(activity as MainActivityEventListener).hideBottomSheet()
//			}
//			false
//		}
//	}
	
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

//			for (checkIn in checkIns) {
//				val latLng = LatLng(checkIn.latitude, checkIn.longitude)
//				polylineOptions.add(latLng)
//				checkInMarkers.add(map.addMarker(MarkerOptions()
//						.position(latLng)
//						.anchor(0.5f, 0.5f)
//						.title(checkIn.time.toFullDateTimeString())
//						.snippet("${checkIn.latitude},${checkIn.latitude}")
//						.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_chek_in_pin_on_map))))
//				checkInPolyline = map.addPolyline(polylineOptions)
//			}
			
			if (checkIns.isNotEmpty()) {
				val lastCheckIn = checkIns.last()
				moveMapTo(LatLng(lastCheckIn.latitude, lastCheckIn.longitude))
			}
		})
	}
	
	private fun moveMapTo(latLng: LatLng) {
		if (!isAdded || isDetached) return
		mapBoxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latLng.latitude, latLng.longitude), 10.0))
	}
	
	fun moveToReportMarker(report: Report) {
//		val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
//				LatLng(report.latitude, report.longitude), googleMap?.cameraPosition?.zoom ?: 18f)
//		googleMap?.animateCamera(cameraUpdate)
		
	}
	
	private fun showLocationMessageError(msg: String) {
		tvAlertTitle.text = msg
		layoutAlertAirplaneMode.visibility = View.VISIBLE
	}
	
	companion object {
		fun newInstance(): MapFragment {
			return MapFragment()
		}
		
		const val MAPBOX_ACCESS_TOKEN = "pk.eyJ1IjoicmF0cmVlMDEiLCJhIjoiY2s4dThnNnNhMDhmcjNtbXpucnhicjQ0aSJ9.eDupWJNzrohc0-rmPPoC6Q"
		const val tag = "MapFragment"
	}
}