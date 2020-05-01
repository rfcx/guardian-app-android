package org.rfcx.ranger.view.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import com.google.gson.JsonPrimitive
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.offline.OfflineManager
import com.mapbox.mapboxsdk.offline.OfflineRegion
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition
import com.mapbox.mapboxsdk.plugins.annotation.LineManager
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import kotlinx.android.synthetic.main.fragment_map.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.location.CheckIn
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.service.AirplaneModeReceiver
import org.rfcx.ranger.util.*
import org.rfcx.ranger.view.MainActivityEventListener
import org.rfcx.ranger.view.base.BaseFragment

class MapFragment : BaseFragment() {
	
	private val mapViewModel: MapViewModel by viewModel()
	private var routeLocations: MutableList<Location> = mutableListOf()
	private val locationPermissions by lazy { activity?.let { LocationPermissions(it) } }
	private var locationManager: LocationManager? = null
	private var lastLocation: Location? = null
	private val analytics by lazy { context?.let { Analytics(it) } }
	private lateinit var mapView: MapView
	private lateinit var mapBoxMap: MapboxMap
	private var currentStyle: String = Style.OUTDOORS
	private var reportList: ArrayList<Report> = arrayListOf()
	private var checkInList: ArrayList<CheckIn> = arrayListOf()
	
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
	
	private val airplaneModeReceiver = AirplaneModeReceiver(onAirplaneModeCallback)
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_map, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		mapView = view.findViewById(R.id.mapView)
		mapView.onCreate(savedInstanceState)
		mapView.getMapAsync { mapboxMap ->
			val symbolLayerIconFeatureList = mutableListOf<Feature>(
					Feature.fromGeometry(Point.fromLngLat(-57.225365, -33.213144)),
					Feature.fromGeometry(Point.fromLngLat(-54.14164, -33.981818)),
					Feature.fromGeometry(Point.fromLngLat(-56.990533, -30.583266))
			)
			val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_chek_in_pin_on_map, null)
			val mBitmap = BitmapUtils.getBitmapFromDrawable(drawable)
			if (mBitmap != null) {
				mapboxMap.setStyle(
						Style.Builder()
								.withImage(
										ICON_ID, mBitmap
								)
								.withSource(
										GeoJsonSource(
												SOURCE_ID,
												FeatureCollection.fromFeatures(symbolLayerIconFeatureList)
										)
								)
								.withLayer(
										SymbolLayer(LAYER_ID, SOURCE_ID)
												.withProperties(
														PropertyFactory.iconImage(ICON_ID),
														PropertyFactory.iconAllowOverlap(true),
														PropertyFactory.iconOffset(arrayOf(0f, 0.9f))
												)
								)
				) {
				}
				
				moveMapTo(LatLng(-33.213144, -57.225365), mapboxMap)
				
			}
		}
		
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
			listAllOfflineMapRegion()
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

//	override fun onMapReady(mapboxMap: MapboxMap) {
//		mapBoxMap = mapboxMap
//		mapboxMap.setStyle(currentStyle) {
//			switchMap(mapboxMap)
//			checkThenAccquireLocation()
//			setDisplay()
//		}
//	}
	
	private fun switchMap(mapboxMap: MapboxMap) {
		switchButton.setOnClickListener {
			currentStyle = if (currentStyle == Style.OUTDOORS) {
				mapboxMap.setStyle(Style.SATELLITE)
				Style.SATELLITE
			} else {
				mapboxMap.setStyle(Style.OUTDOORS)
				Style.OUTDOORS
			}
//			onMapReady(mapboxMap)
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
		displayCheckIn()
		displayReport(mapBoxMap)
		
		if (reportList.isEmpty() && checkInList.isEmpty()) {
			getCurrentLocation(mapBoxMap)
		}
		
		if (!context.isNetworkAvailable()) {
			listAllOfflineMapRegion()
			switchButton.visibility = View.GONE
		} else {
			switchButton.visibility = View.VISIBLE
		}
	}
	
	private fun displayReport(mapboxMap: MapboxMap) {
		val symbolManager = mapboxMap.style?.let { SymbolManager(mapView, mapBoxMap, it) }
		symbolManager?.iconAllowOverlap = true
		symbolManager?.iconIgnorePlacement = true
		
		mapViewModel.getReports().observe(this, Observer { reports ->
			reportList = arrayListOf()
			
			for (report in reports) {
				if (!isAdded || isDetached) return@Observer
				reports.map { reportList.add(it) }
				
				val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_pin_map, null)
				val mBitmap = BitmapUtils.getBitmapFromDrawable(drawable)
				if (mBitmap != null) {
					mapBoxMap.style?.addImage("pin-map", mBitmap)
				}
				
				
				symbolManager?.create(SymbolOptions()
						.withLatLng(LatLng(report.latitude, report.longitude))
						.withIconImage("pin-map")
						.withIconSize(1.0f)
						.withData(JsonPrimitive(report.id)))
			}
			
			if (reports.isNotEmpty()) {
				val lastCheckIn = reportList.last()
//				moveMapTo(LatLng(lastCheckIn.latitude, lastCheckIn.longitude))
			}
			
			symbolManager?.addClickListener { symbol ->
				(activity as MainActivityEventListener).showBottomSheet(ReportViewPagerFragment.newInstance(symbol.data.toString().toInt()))
			}
			
		})
		
	}
	
	private fun displayCheckIn() {
		val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_chek_in_pin_on_map, null)
		val mBitmap = BitmapUtils.getBitmapFromDrawable(drawable)
		if (mBitmap != null) {
			mapBoxMap.style?.addImage("check_in_pin", mBitmap)
		}
		
		mapViewModel.getCheckIns().observe(this, Observer { checkIns ->
			val location = Location(LocationManager.GPS_PROVIDER)
			
			if (!isAdded || isDetached) return@Observer
			checkInList = arrayListOf()
			checkIns.map { checkInList.add(it) }
			
			for (checkIn in checkIns) {
				location.latitude = checkIn.latitude
				location.longitude = checkIn.longitude
				routeLocations.add(location)
				
				val symbolManager = mapBoxMap.style?.let { SymbolManager(mapView, mapBoxMap, it) }
				symbolManager?.iconAllowOverlap = true
				symbolManager?.iconIgnorePlacement = true
				
				symbolManager?.create(SymbolOptions()
						.withLatLng(LatLng(checkIn.latitude, checkIn.longitude))
						.withIconImage("check_in_pin")
						.withIconSize(1.0f))
				
			}
			addCheckIn()
		})
	}
	
	private fun addCheckIn() {
		val lineManager = mapBoxMap.style?.let { LineManager(mapView, mapBoxMap, it) }
		lineManager?.let { line ->
			line.deleteAll()
			
			val sortedLocations = routeLocations
					.sortedBy { location -> location.time }
					.map { location -> Point.fromLngLat(location.longitude, location.latitude) }
			
			line.create(LineOptions()
					.withLineColor("#969faa")
					.withLineWidth(5.0f)
					.withGeometry(LineString.fromLngLats(sortedLocations))
			)
		}
		
		if (checkInList.isNotEmpty()) {
			val lastCheckIn = checkInList.last()
//			moveMapTo(LatLng(lastCheckIn.latitude, lastCheckIn.longitude))
		}
	}
	
	private fun listAllOfflineMapRegion() {
		val offlineManager = context?.let { OfflineManager.getInstance(it) }
		offlineManager?.listOfflineRegions(object : OfflineManager.ListOfflineRegionsCallback {
			override fun onList(offlineRegions: Array<out OfflineRegion>?) {
				if (offlineRegions?.size != null) {
					if (offlineRegions.isNotEmpty()) {
						val bounds = (offlineRegions[offlineRegions.size - 1].definition as OfflineTilePyramidRegionDefinition).bounds
						val regionZoom = (offlineRegions[offlineRegions.size - 1].definition as OfflineTilePyramidRegionDefinition).minZoom
						
						val cameraPosition = CameraPosition.Builder()
								.target(bounds.center)
								.zoom(regionZoom)
								.build()
						mapBoxMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
					}
				}
			}
			
			override fun onError(error: String?) {
			
			}
		})
	}
	
	private fun moveCameraToCurrentLocation(location: Location) {
		lastLocation = location
		val latLng = LatLng(location.latitude, location.longitude)
		mapBoxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0))
	}
	
	private fun moveMapTo(latLng: LatLng, mapBoxMap: MapboxMap) {
		if (!isAdded || isDetached) return
		mapBoxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latLng.latitude, latLng.longitude), 5.0))
	}
	
	fun moveToReportMarker(report: Report) {
		mapBoxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(report.latitude, report.longitude), mapBoxMap.cameraPosition.zoom))
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
		
		private const val SOURCE_ID = "SOURCE_ID"
		private const val ICON_ID = "ICON_ID"
		private const val LAYER_ID = "LAYER_ID"
	}
}