package org.rfcx.ranger.view.events

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.fragment_new_events.*
import kotlinx.android.synthetic.main.toolbar_project.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.entity.project.Project
import org.rfcx.ranger.util.toJsonObject
import org.rfcx.ranger.view.MainActivityEventListener
import org.rfcx.ranger.view.events.adapter.EventGroup
import org.rfcx.ranger.view.events.adapter.GuardianItemAdapter
import org.rfcx.ranger.view.project.ProjectAdapter
import org.rfcx.ranger.view.project.ProjectOnClickListener

class EventsFragment : Fragment(), OnMapReadyCallback, PermissionsListener, ProjectOnClickListener, (EventGroup) -> Unit {
	
	companion object {
		const val tag = "EventsFragment"
		
		private const val COUNT = "count"
		private const val CLUSTER = "cluster"
		private const val BUILDING = "building"
		private const val POINT_COUNT = "point_count"
		private const val SOURCE_ALERT = "source.alert"
		private const val PROPERTY_MARKER_ALERT_SITE = "alert.site"
		private const val UN_CLUSTERED_POINTS = "un-clustered-points"
		private const val DEFAULT_MAP_ZOOM = 15.0
		private const val PADDING_BOUNDS = 230
		private const val DURATION_MS = 1300
		
		@JvmStatic
		fun newInstance() = EventsFragment()
	}
	
	private val viewModel: EventsViewModel by viewModel()
	private val projectAdapter by lazy { ProjectAdapter(this) }
	private val nearbyAdapter by lazy { GuardianItemAdapter(this) }
	private val othersAdapter by lazy { GuardianItemAdapter(this) }
	
	private lateinit var mapView: MapView
	private var mapBoxMap: MapboxMap? = null
	private var locationManager: LocationManager? = null
	private var lastLocation: Location? = null
	private var permissionsManager: PermissionsManager = PermissionsManager(this)
	private val locationListener = object : android.location.LocationListener {
		override fun onLocationChanged(p0: Location) {
			moveCameraToCurrentLocation(p0)
			if (PermissionsManager.areLocationPermissionsGranted(context)) {
				mapBoxMap?.style?.let { style -> enableLocationComponent(style) }
			}
		}
		
		override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
		override fun onProviderEnabled(p0: String) {}
		override fun onProviderDisabled(p0: String) {}
	}
	
	private var alertSource: GeoJsonSource? = null
	private var alertFeatures: FeatureCollection? = null
	
	private var queryLayerIds: Array<String> = arrayOf()
	private var isShowMapIcon = true
	lateinit var listener: MainActivityEventListener
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		listener = (context as MainActivityEventListener)
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		context?.let { Mapbox.getInstance(it, getString(R.string.mapbox_token)) }
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
	                          savedInstanceState: Bundle?): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_new_events, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		mapView = view.findViewById(R.id.mapView)
		mapView.onCreate(savedInstanceState)
		mapView.getMapAsync(this)
		
		setupToolbar()
		viewModel.fetchProjects()
		setOnClickListener()
		setObserver()
	}
	
	private fun setRecyclerView() {
		projectRecyclerView.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = projectAdapter
			projectAdapter.items = viewModel.getProjectsFromLocal()
		}
		setProjectTitle(viewModel.getProjectName())
		
		nearbyRecyclerView.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = nearbyAdapter
			nearbyAdapter.items = viewModel.nearbyGuardians
		}
		
		nearbyLayout.visibility = if (viewModel.nearbyGuardians.isEmpty()) View.GONE else View.VISIBLE
		othersTextView.visibility = if (viewModel.nearbyGuardians.isEmpty()) View.GONE else View.VISIBLE
		
		othersRecyclerView.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = othersAdapter
			othersAdapter.items = viewModel.othersGuardians
		}
	}
	
	private fun setOnClickListener() {
		projectTitleLayout.setOnClickListener {
			setOnClickProjectName()
		}
		
		projectSwipeRefreshView.apply {
			setOnRefreshListener {
				viewModel.fetchProjects()
				isRefreshing = true
			}
			setColorSchemeResources(R.color.colorPrimary)
		}
	}
	
	private fun setOnClickProjectName() {
		listener.hideBottomAppBar()
		projectRecyclerView.visibility = View.VISIBLE
		projectSwipeRefreshView.visibility = View.VISIBLE
	}
	
	override fun onClicked(project: Project) {
		listener.showBottomAppBar()
		projectRecyclerView.visibility = View.GONE
		projectSwipeRefreshView.visibility = View.GONE
		viewModel.setProjectSelected(project.id)
		setProjectTitle(project.name)
	}
	
	@SuppressLint("NotifyDataSetChanged")
	private fun setObserver() {
		viewModel.projects.observe(viewLifecycleOwner, { it ->
			it.success({
				projectSwipeRefreshView.isRefreshing = false
				projectAdapter.items = listOf()
				projectAdapter.items = viewModel.getProjectsFromLocal()
				projectAdapter.notifyDataSetChanged()
			}, {
				projectSwipeRefreshView.isRefreshing = false
				Toast.makeText(context, it.message
						?: getString(R.string.something_is_wrong), Toast.LENGTH_LONG).show()
			}, {
			})
		})
		
		// observe alerts
		viewModel.getAlerts().observe(viewLifecycleOwner, { alerts ->
			val features = alerts.map {
				val properties = mapOf(Pair(PROPERTY_MARKER_ALERT_SITE, it.guardianName))
				Feature.fromGeometry(Point.fromLngLat(it.longitude ?: 0.0, it.latitude
						?: 0.0), properties.toJsonObject())
			}
			alertFeatures = FeatureCollection.fromFeatures(features)
			refreshSource()
		})
	}
	
	private fun setProjectTitle(str: String) {
		projectTitleTextView.text = str
	}
	
	override fun onLockImageClicked() {
		Toast.makeText(context, R.string.not_have_permission, Toast.LENGTH_LONG).show()
	}
	
	override fun invoke(guardian: EventGroup) {
		listener.openGuardianEventDetail(guardian)
	}
	
	private fun setupToolbar() {
		(activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
		
		changePageImageView.setOnClickListener {
			if (isShowMapIcon) {
				changePageImageView.setImageResource(R.drawable.ic_view_list)
				mapView.visibility = View.VISIBLE
				guardianListScrollView.visibility = View.GONE
				mapBoxMap?.style?.let { style -> enableLocationComponent(style) }
			} else {
				changePageImageView.setImageResource(R.drawable.ic_map)
				mapView.visibility = View.GONE
				guardianListScrollView.visibility = View.VISIBLE
			}
			isShowMapIcon = !isShowMapIcon
		}
	}
	
	/* ------------------- vv Setup Map vv ------------------- */
	
	override fun onMapReady(mapboxMap: MapboxMap) {
		mapBoxMap = mapboxMap
		mapboxMap.setStyle(Style.OUTDOORS) { style ->
			mapboxMap.uiSettings.isAttributionEnabled = false
			mapboxMap.uiSettings.isLogoEnabled = false
			getLocation()
			setupSources(style)
			refreshSource()
			addClusteredGeoJsonSource(style)
			alertFeatures?.let { moveCameraToLeavesBounds(it) }
			lastLocation?.let { viewModel.handledGuardians(it) }
			setRecyclerView()
		}
		
		mapboxMap.addOnMapClickListener { latLng ->
			handleClickIcon(mapboxMap.projection.toScreenLocation(latLng))
		}
	}
	
	private fun handleClickIcon(screenPoint: PointF): Boolean {
		val rectF = RectF(screenPoint.x - 10, screenPoint.y - 10, screenPoint.x + 10, screenPoint.y + 10)
		var alertFeatures = listOf<Feature>()
		queryLayerIds.forEach {
			val features = mapBoxMap?.queryRenderedFeatures(rectF, it) ?: listOf()
			if (features.isNotEmpty()) {
				alertFeatures = features
			}
		}
		
		if (alertFeatures.isNotEmpty()) {
			val pinCount = if (alertFeatures[0].getProperty(POINT_COUNT) != null) alertFeatures[0].getProperty(POINT_COUNT).asInt else 0
			if (pinCount > 1) {
				val clusterLeavesFeatureCollection = alertSource?.getClusterLeaves(alertFeatures[0], 8000, 0)
				val features = clusterLeavesFeatureCollection?.features()
				if (clusterLeavesFeatureCollection != null) {
					if (features?.groupBy { it }?.size == 1) {
						Toast.makeText(context, features[0].getProperty(PROPERTY_MARKER_ALERT_SITE).asString, Toast.LENGTH_SHORT).show()
					} else {
						moveCameraToLeavesBounds(clusterLeavesFeatureCollection)
					}
				}
			} else {
				val selectedFeature = alertFeatures[0]
				Toast.makeText(context, selectedFeature.getProperty(PROPERTY_MARKER_ALERT_SITE).asString, Toast.LENGTH_SHORT).show()
			}
			return true
		}
		return false
	}
	
	private fun setupSources(it: Style) {
		alertSource = GeoJsonSource(SOURCE_ALERT, FeatureCollection.fromFeatures(listOf()), GeoJsonOptions()
				.withCluster(true)
				.withClusterMaxZoom(15)
				.withClusterRadius(20))
		it.addSource(alertSource!!)
	}
	
	private fun refreshSource() {
		if (alertSource != null && alertFeatures != null) {
			alertSource!!.setGeoJson(alertFeatures)
		}
	}
	
	private fun addClusteredGeoJsonSource(style: Style) {
		val layers = Array(1) { IntArray(2) }
		layers[0] = intArrayOf(0, Color.parseColor("#e41a1a"))
		
		queryLayerIds = Array(layers.size) { "" }
		
		layers.forEachIndexed { index, layer ->
			queryLayerIds[index] = "cluster-$index"
			val circles = CircleLayer(queryLayerIds[index], SOURCE_ALERT)
			circles.setProperties(PropertyFactory.circleColor(layer[1]), PropertyFactory.circleRadius(10f))
			val pointCount = Expression.toNumber(Expression.get(POINT_COUNT))
			circles.setFilter(
					if (index == 0)
						Expression.gte(pointCount, Expression.literal(layer[0])) else
						Expression.all(
								Expression.gte(pointCount, Expression.literal(layer[0])),
								Expression.lt(pointCount, Expression.literal(layers[index - 1][0]))
						)
			)
			style.addLayerBelow(circles, BUILDING)
		}
		
		val count = SymbolLayer(COUNT, SOURCE_ALERT)
		count.setProperties(
				PropertyFactory.textField(Expression.toString(Expression.get(POINT_COUNT))),
				PropertyFactory.textSize(12f),
				PropertyFactory.textColor(Color.WHITE),
				PropertyFactory.textIgnorePlacement(true),
				PropertyFactory.textAllowOverlap(true)
		)
		style.addLayer(count)
		
		val unClustered = CircleLayer(UN_CLUSTERED_POINTS, SOURCE_ALERT)
		unClustered.setProperties(PropertyFactory.circleColor(Color.parseColor("#e41a1a")), PropertyFactory.circleRadius(10f), PropertyFactory.circleBlur(1f))
		unClustered.setFilter(Expression.neq(Expression.get(CLUSTER), Expression.literal(true)))
		style.addLayerBelow(unClustered, BUILDING)
	}
	
	private fun enableLocationComponent(style: Style) {
		val context = context ?: return
		val mapboxMap = mapBoxMap ?: return
		
		// Check if permissions are enabled and if not request
		if (PermissionsManager.areLocationPermissionsGranted(context)) {
			
			// Create and customize the LocationComponent's options
			val customLocationComponentOptions = LocationComponentOptions.builder(context)
					.trackingGesturesManagement(true)
					.accuracyColor(ContextCompat.getColor(context, R.color.colorPrimary))
					.build()
			
			val locationComponentActivationOptions = LocationComponentActivationOptions.builder(context, style)
					.locationComponentOptions(customLocationComponentOptions)
					.build()
			
			// Get an instance of the LocationComponent and then adjust its settings
			mapboxMap.locationComponent.apply {
				// Activate the LocationComponent with options
				activateLocationComponent(locationComponentActivationOptions)
				// Enable to make the LocationComponent visible
				if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
						&& ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					return
				}
				isLocationComponentEnabled = true
				
				// Set the LocationComponent's camera mode
				cameraMode = CameraMode.TRACKING
				// Set the LocationComponent's render mode
				renderMode = RenderMode.COMPASS
			}
		} else {
			permissionsManager = PermissionsManager(this)
			permissionsManager.requestLocationPermissions(activity)
		}
	}
	
	private fun getLocation() {
		if (!isAdded || isDetached) return
		val style = mapBoxMap?.style ?: return
		
		// Check if permissions are enabled and if not request
		if (PermissionsManager.areLocationPermissionsGranted(context)) {
			locationManager?.removeUpdates(locationListener)
			locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
			try {
				lastLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
				lastLocation?.let { moveCameraToCurrentLocation(it) }
				enableLocationComponent(style)
			} catch (ex: SecurityException) {
				ex.printStackTrace()
			} catch (ex: IllegalArgumentException) {
				ex.printStackTrace()
			}
		} else {
			permissionsManager = PermissionsManager(this)
			permissionsManager.requestLocationPermissions(activity)
		}
	}
	
	private fun moveCameraToCurrentLocation(location: Location) {
		lastLocation = location
		moveCameraTo(LatLng(location.latitude, location.longitude))
	}
	
	private fun moveCameraTo(latLng: LatLng) {
		mapBoxMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_MAP_ZOOM))
	}
	
	private fun moveCameraToLeavesBounds(featureCollectionToInspect: FeatureCollection) {
		val latLngList: ArrayList<LatLng> = ArrayList()
		if (featureCollectionToInspect.features() != null) {
			for (singleClusterFeature in featureCollectionToInspect.features()!!) {
				val clusterPoint = singleClusterFeature.geometry() as Point?
				if (clusterPoint != null) {
					latLngList.add(LatLng(clusterPoint.latitude(), clusterPoint.longitude()))
				}
			}
			if (latLngList.size > 1) {
				moveCameraWithLatLngList(latLngList)
			}
		}
	}
	
	private fun moveCameraWithLatLngList(latLngList: List<LatLng>) {
		val latLngBounds = LatLngBounds.Builder()
				.includes(latLngList)
				.build()
		mapBoxMap?.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, PADDING_BOUNDS), DURATION_MS)
	}
	
	override fun onResume() {
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
	
	override fun onPause() {
		super.onPause()
		mapView.onPause()
	}
	
	override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {}
	
	override fun onPermissionResult(granted: Boolean) {
		val style = mapBoxMap?.style ?: return
		val context = context ?: return
		if (granted) {
			enableLocationComponent(style)
		} else {
			Toast.makeText(context, R.string.location_permission_msg, Toast.LENGTH_LONG).show()
		}
	}
}
