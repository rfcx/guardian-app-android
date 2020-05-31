package org.rfcx.ranger.view.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.PointF
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.BubbleLayout
import com.mapbox.mapboxsdk.camera.CameraPosition
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
import com.mapbox.mapboxsdk.offline.OfflineManager
import com.mapbox.mapboxsdk.offline.OfflineRegion
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.layout_map_window_info.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.location.CheckIn
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.service.AirplaneModeReceiver
import org.rfcx.ranger.util.*
import org.rfcx.ranger.view.MainActivityEventListener
import org.rfcx.ranger.view.base.BaseFragment

class MapFragment : BaseFragment(), OnMapReadyCallback {
	
	private val mapViewModel: MapViewModel by viewModel()
	private val locationPermissions by lazy { activity?.let { LocationPermissions(it) } }
	private var locationManager: LocationManager? = null
	private var lastLocation: Location? = null
	private val analytics by lazy { context?.let { Analytics(it) } }
	private lateinit var mapView: MapView
	private var mapBoxMap: MapboxMap? = null
	private var currentStyle: String = Style.OUTDOORS
	private var reports: List<Report> = listOf()
	private var checkins: List<CheckIn> = listOf()
	private var reportSource: GeoJsonSource? = null
	private var checkInSource: GeoJsonSource? = null
	private var reportFeatures: FeatureCollection? = null
	private var checkInFeatures: FeatureCollection? = null
	
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
		context?.let { Mapbox.getInstance(it, getString(R.string.mapbox_token)) }
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
		mapView.getMapAsync(this)
		
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
		mapboxMap.setStyle(currentStyle) {
			setupSources(it)
			setupImages(it)
			setupMarkerLayers(it)
			setupLineLayer(it)
			setupWindowInfo(it)
			observeData()
			checkThenAccquireLocation()
			setupSwitchMapMode()
			mapboxMap.addOnMapClickListener { latLng ->
				handleClickIcon(mapboxMap.projection.toScreenLocation(latLng))
			}
		}
	}
	
	private fun handleClickIcon(screenPoint: PointF): Boolean {
		val reportFeatures = mapBoxMap?.queryRenderedFeatures(screenPoint, MARKER_REPORT_ID)
		val checkInFeatures = mapBoxMap?.queryRenderedFeatures(screenPoint, MARKER_CHECK_IN_ID)
		if (reportFeatures != null && reportFeatures.isNotEmpty()) {
			clearCheckInFeatureSelected()
			val selectedFeature = reportFeatures[0]
			val features = this.reportFeatures!!.features()!!
			features.forEachIndexed { index, feature ->
				if (selectedFeature.getProperty(PROPERTY_MARKER_REPORT_ID) == feature.getProperty(PROPERTY_MARKER_REPORT_ID)) {
					features[index]?.let { setFeatureSelectState(it, true) }
					val reportId = selectedFeature.getStringProperty(PROPERTY_MARKER_REPORT_ID)
					(activity as MainActivityEventListener).showBottomSheet(ReportViewPagerFragment.newInstance(reportId.toInt()))
				} else {
					features[index]?.let { setFeatureSelectState(it, false) }
				}
			}
			return true
		}
		
		(activity as MainActivityEventListener).hideBottomSheet()
		
		if (checkInFeatures != null && checkInFeatures.isNotEmpty()) {
			val selectedFeature = checkInFeatures[0]
			val features = this.checkInFeatures!!.features()!!
			features.forEachIndexed { index, feature ->
				if (selectedFeature.getProperty(PROPERTY_MARKER_CHECKIN_ID) == feature.getProperty(PROPERTY_MARKER_CHECKIN_ID)) {
					features[index]?.let { setFeatureSelectState(it, true) }
				} else {
					features[index]?.let { setFeatureSelectState(it, false) }
				}
			}
			return true
		}
		
		clearFeatureSelected()
		return false
	}
	
	private fun clearFeatureSelected() {
		if (this.checkInFeatures?.features() != null && this.reportFeatures?.features() != null) {
			val features = this.checkInFeatures!!.features()!! + this.reportFeatures!!.features()!!
			features.forEach { setFeatureSelectState(it, false) }
		}
	}
	
	private fun clearCheckInFeatureSelected() {
		if (this.checkInFeatures?.features() != null) {
			val features = this.checkInFeatures!!.features()!!
			features.forEach { setFeatureSelectState(it, false) }
		}
	}
	
	private fun setFeatureSelectState(feature: Feature, selectedState: Boolean) {
		feature.properties()?.let {
			it.addProperty(PROPERTY_SELECTED, selectedState)
			refreshSource()
		}
	}
	
	private fun setupSources(it: Style) {
		reportSource = GeoJsonSource(SOURCE_REPORT, FeatureCollection.fromFeatures(listOf()))
		it.addSource(reportSource!!)
		
		checkInSource = GeoJsonSource(SOURCE_CHECK_IN, FeatureCollection.fromFeatures(listOf()))
		it.addSource(checkInSource!!)
	}
	
	private fun setupImages(it: Style) {
		// Setup report image pin
		val reportDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_pin_map, null)
		val reportBitmap = BitmapUtils.getBitmapFromDrawable(reportDrawable)
		if (reportBitmap != null) {
			it.addImage(MARKER_REPORT_IMAGE, reportBitmap)
		}
		
		// Setup checkin image pin
		val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_chek_in_pin_on_map, null)
		val mBitmap = BitmapUtils.getBitmapFromDrawable(drawable)
		if (mBitmap != null) {
			it.addImage(MARKER_CHECK_IN_IMAGE, mBitmap)
		}
	}
	
	private fun refreshSource() {
		if (reportSource != null && reportFeatures != null) {
			reportSource!!.setGeoJson(reportFeatures)
		}
		
		if (checkInSource != null && checkInFeatures != null) {
			checkInSource!!.setGeoJson(checkInFeatures)
		}
	}
	
	private fun setupMarkerLayers(it: Style) {
		val reportLayer = SymbolLayer(MARKER_REPORT_ID, SOURCE_REPORT).apply {
			withProperties(
					PropertyFactory.iconImage(MARKER_REPORT_IMAGE),
					PropertyFactory.iconAllowOverlap(true),
					PropertyFactory.iconIgnorePlacement(true),
					PropertyFactory.iconOffset(arrayOf(0f, -9f)),
					PropertyFactory.iconSize(1f)
			)
		}
		
		val checkInLayer = SymbolLayer(MARKER_CHECK_IN_ID, SOURCE_CHECK_IN).apply {
			withProperties(
					PropertyFactory.iconImage(MARKER_CHECK_IN_IMAGE),
					PropertyFactory.iconAllowOverlap(true),
					PropertyFactory.iconIgnorePlacement(true),
					PropertyFactory.iconSize(1f)
			)
		}
		it.addLayer(reportLayer)
		it.addLayer(checkInLayer)
	}
	
	private fun setupLineLayer(it: Style) {
		it.addLayer(LineLayer(LINE_CHECK_IN_ID, SOURCE_CHECK_IN).apply {
			withProperties(
					PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
					PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
					PropertyFactory.lineWidth(4f),
					context?.let { it1 ->
						PropertyFactory.lineColor(ContextCompat.getColor(it1, R.color.grey_default))
					}
			)
		})
	}
	
	private fun setupWindowInfo(it: Style) {
		it.addLayer(SymbolLayer(WINDOW_INFO_CHECK_IN_ID, SOURCE_CHECK_IN).apply {
			withProperties(
					PropertyFactory.iconImage("{$PROPERTY_MARKER_CHECKIN_ID}"),
					PropertyFactory.iconAnchor(ICON_ANCHOR_BOTTOM),
					PropertyFactory.iconAllowOverlap(true)
			)
			withFilter(eq(get(PROPERTY_SELECTED), literal(true)))
		})
	}
	
	private fun observeData() {
		// observe reports
		mapViewModel.getReports().observe(this, Observer { reports ->
			this.reports = reports
			val features = reports.map {
				// example: https://raw.githubusercontent.com/mapbox/mapbox-android-demo/master/MapboxAndroidDemo/src/main/assets/us_west_coast.geojson
				val properties = mapOf(Pair(PROPERTY_MARKER_REPORT_ID, it.id.toString()))
				Feature.fromGeometry(Point.fromLngLat(it.longitude, it.latitude), properties.toJsonObject())
			}
			reportFeatures = FeatureCollection.fromFeatures(features)
			
			refreshSource()
			
			// Set zoom to last lastReport
			if (reports.isNotEmpty()) {
				val lastReport = this.reports.last()
				moveMapTo(LatLng(lastReport.latitude, lastReport.longitude))
			}
		})
		
		// observe check-ins
		mapViewModel.getCheckIns().observe(this, Observer { checkins ->
			this.checkins = checkins
			
			// Create point
			val pointFeatures = checkins.map {
				val properties = mapOf(
						Pair(PROPERTY_MARKER_CHECKIN_ID, "$PROPERTY_MARKER_CHECKIN_ID.${it.id}"),
						Pair(PROPERTY_MARKER_TITLE, it.time.toFullDateTimeString()),
						Pair(PROPERTY_MARKER_CAPTION, it.getLatLng())
				)
				Feature.fromGeometry(Point.fromLngLat(it.longitude, it.latitude), properties.toJsonObject())
			}
			
			// Create line
			val pointLine = checkins.map {
				Point.fromLngLat(it.longitude, it.latitude)
			}
			val lineFeature = Feature.fromGeometry(LineString.fromLngLats(pointLine))
			
			// Set zoom to last lastCheckIn
			if (checkins.isNotEmpty()) {
				val lastCheckIn = checkins.last()
				moveMapTo(LatLng(lastCheckIn.latitude, lastCheckIn.longitude))
			}
			
			// Create window info
			val windowInfoImages = hashMapOf<String, Bitmap>()
			val inflater = LayoutInflater.from(activity)
			pointFeatures.forEach {
				val bubbleLayout = inflater.inflate(R.layout.layout_map_window_info, null) as BubbleLayout
				
				val id = it.getStringProperty(PROPERTY_MARKER_CHECKIN_ID)
				
				val title = it.getStringProperty(PROPERTY_MARKER_TITLE)
				bubbleLayout.infoWindowTitle.text = title
				
				val caption = it.getStringProperty(PROPERTY_MARKER_CAPTION)
				bubbleLayout.infoWindowDescription.text = caption
				
				val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
				bubbleLayout.measure(measureSpec, measureSpec)
				val measuredWidth = bubbleLayout.measuredWidth
				bubbleLayout.arrowPosition = (measuredWidth / 2 - 5).toFloat()
				
				val bitmap = SymbolGenerator.generate(bubbleLayout)
				windowInfoImages[id] = bitmap
			}
			
			setWindowInfoImageGenResults(windowInfoImages)
			checkInFeatures = FeatureCollection.fromFeatures(pointFeatures + lineFeature)
			refreshSource()
		})
	}
	
	private fun setWindowInfoImageGenResults(windowInfoImages: HashMap<String, Bitmap>) {
		mapBoxMap?.style?.addImages(windowInfoImages)
	}
	
	private fun setupSwitchMapMode() {
		switchButton.setOnClickListener {
			currentStyle = if (currentStyle == Style.OUTDOORS) {
				Style.SATELLITE
			} else {
				Style.OUTDOORS
			}
			updateMapStyle(currentStyle)
		}
	}
	
	private fun updateMapStyle(currentStyle: String) {
		mapBoxMap?.setStyle(currentStyle)
	}
	
	private fun getCurrentLocation(mapboxMap: MapboxMap?) {
		context?.let {
			val customLocationComponentOptions = LocationComponentOptions.builder(it)
					.trackingGesturesManagement(true)
					.accuracyColor(ContextCompat.getColor(it, R.color.colorPrimary))
					.build()
			
			val locationComponentActivationOptions = mapboxMap?.style?.let { style ->
				LocationComponentActivationOptions.builder(it, style)
						.locationComponentOptions(customLocationComponentOptions)
						.build()
			}
			
			mapboxMap?.let { mapboxMap ->
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
	}
	
	private fun checkThenAccquireLocation() {
		if (context?.isOnAirplaneMode()!!) {
			showLocationMessageError("${getString(R.string.in_air_plane_mode)} \n ${getString(R.string.pls_off_air_plane_mode)}")
		} else {
			locationPermissions?.check { isAllowed: Boolean ->
				if (isAllowed) {
					getLocation()
				} else {
					setDisplayTools()
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
			setDisplayTools()
		} catch (ex: SecurityException) {
			ex.printStackTrace()
		} catch (ex: IllegalArgumentException) {
			ex.printStackTrace()
		}
	}
	
	private fun setDisplayTools() {
		getCurrentLocation(mapBoxMap)
		
		if (!context.isNetworkAvailable()) {
			listAllOfflineMapRegion()
			switchButton.visibility = View.GONE
		} else {
			switchButton.visibility = View.VISIBLE
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
						mapBoxMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
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
		mapBoxMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0))
	}
	
	private fun moveMapTo(latLng: LatLng) {
		mapBoxMap?.let {
			it.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latLng.latitude,
					latLng.longitude), it.cameraPosition.zoom))
		}
	}
	
	fun moveToReportMarker(report: Report) {
		mapBoxMap?.let {
			it.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(report.latitude,
					report.longitude), it.cameraPosition.zoom))
		}
	}
	
	private fun showLocationMessageError(msg: String) {
		tvAlertTitle.text = msg
		layoutAlertAirplaneMode.visibility = View.VISIBLE
	}
	
	companion object {
		fun newInstance(): MapFragment {
			return MapFragment()
		}
		
		const val tag = "MapFragment"
		
		private const val SOURCE_CHECK_IN = "source.checkin"
		private const val MARKER_CHECK_IN_ID = "marker.checkin"
		private const val MARKER_CHECK_IN_IMAGE = "marker.checkin.pin"
		private const val LINE_CHECK_IN_ID = "line.checkin"
		private const val SOURCE_REPORT = "source.report"
		private const val MARKER_REPORT_ID = "marker.report"
		private const val MARKER_REPORT_IMAGE = "marker.report.pin"
		private const val WINDOW_INFO_CHECK_IN_ID = "windowinfo.checkin"
		
		private const val PROPERTY_SELECTED = "selected"
		private const val PROPERTY_MARKER_TITLE = "title"
		private const val PROPERTY_MARKER_CAPTION = "caption"
		private const val PROPERTY_MARKER_REPORT_ID = "report.id"
		private const val PROPERTY_MARKER_CHECKIN_ID = "checkin.id"
		
	}
}