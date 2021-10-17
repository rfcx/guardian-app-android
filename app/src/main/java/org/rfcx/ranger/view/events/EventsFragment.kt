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
import com.mapbox.geojson.LineString
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
import com.mapbox.mapboxsdk.style.layers.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.fragment_new_events.*
import kotlinx.android.synthetic.main.toolbar_project.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.success
import org.rfcx.ranger.entity.Stream
import org.rfcx.ranger.entity.location.Tracking
import org.rfcx.ranger.entity.project.Project
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.toJsonObject
import org.rfcx.ranger.view.MainActivityEventListener
import org.rfcx.ranger.view.events.adapter.EventGroup
import org.rfcx.ranger.view.events.adapter.GuardianItemAdapter
import org.rfcx.ranger.view.project.ProjectAdapter
import org.rfcx.ranger.view.project.ProjectOnClickListener
import java.util.*
import kotlin.collections.ArrayList

class EventsFragment : Fragment(), OnMapReadyCallback, PermissionsListener, ProjectOnClickListener, (EventGroup) -> Unit {
	
	companion object {
		const val tag = "EventsFragment"
		
		private const val COUNT = "count"
		private const val COUNT_EVENTS = "count.events"
		private const val POINT_COUNT = "point_count"
		private const val SOURCE_ALERT = "source.alert"
		private const val SOURCE_LINE = "source.line"
		private const val PROPERTY_MARKER_ALERT_SITE = "alert.site"
		private const val PROPERTY_MARKER_ALERT_DISTANCE = "alert.distance"
		private const val PROPERTY_MARKER_ALERT_STREAM_ID = "alert.stream.id"
		private const val PROPERTY_MARKER_ALERT_COUNT = "alert.count"
		private const val PROPERTY_CLUSTER_TYPE = "cluster.type"
		private const val PROPERTY_CLUSTER_COUNT_EVENTS = "cluster.count.events"
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
	lateinit var preferences: Preferences
	
	private lateinit var mapView: MapView
	private var mapBoxMap: MapboxMap? = null
	private var locationManager: LocationManager? = null
	private var lastLocation: Location? = null
	private var permissionsManager: PermissionsManager = PermissionsManager(this)
	private val locationListener = object : android.location.LocationListener {
		override fun onLocationChanged(p0: Location) {
			moveCameraToCurrentLocation(p0)
			viewModel.saveLastTimeToKnowTheCurrentLocation(requireContext(), Date().time)
			
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
	
	private var lineSource: GeoJsonSource? = null
	private var lineFeatures: FeatureCollection? = null
	
	private var queryLayerIds: Array<String> = arrayOf()
	private var isShowMapIcon = true
	lateinit var listener: MainActivityEventListener
	private var tracking = Tracking()
	
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
		preferences = Preferences.getInstance(requireContext())
		
		getLocation()
		setupToolbar()
		viewModel.fetchProjects()
		setOnClickListener()
		isShowProgressBar()
		setObserver()
		setRecyclerView()
	}
	
	override fun onHiddenChanged(hidden: Boolean) {
		super.onHiddenChanged(hidden)
		if (!hidden) {
			viewModel.loadStreams()
		}
	}
	
	private fun setRecyclerView() {
		projectRecyclerView.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = projectAdapter
			projectAdapter.items = viewModel.getProjectsFromLocal()
		}
		
		val projectId = preferences.getInt(Preferences.SELECTED_PROJECT, -1)
		setProjectTitle(viewModel.getProjectName(projectId))
		
		nearbyRecyclerView.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = nearbyAdapter
			nearbyAdapter.items = viewModel.nearbyGuardians
		}
		
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
		nearbyLayout.visibility = View.GONE
		othersLayout.visibility = View.GONE
		
		isShowProgressBar()
		nearbyAdapter.items = listOf()
		othersAdapter.items = listOf()
		
		listener.showBottomAppBar()
		projectRecyclerView.visibility = View.GONE
		projectSwipeRefreshView.visibility = View.GONE
		viewModel.setProjectSelected(project.id)
		viewModel.loadStreams()
		setAlertFeatures(viewModel.getStreams())
		setProjectTitle(project.name)
	}
	
	@SuppressLint("NotifyDataSetChanged")
	private fun setObserver() {
		viewModel.getProjectsFromRemote.observe(viewLifecycleOwner, { it ->
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
		
		viewModel.getStreamsFromRemote.observe(viewLifecycleOwner, { it ->
			it.success({ list ->
				viewModel.handledStreams(lastLocation, list)
				isShowProgressBar(false)
				setShowListStream()
				isShowNotHaveStreams(viewModel.nearbyGuardians.isEmpty() && viewModel.othersGuardians.isEmpty() && mapView.visibility == View.GONE)
				nearbyAdapter.items = viewModel.nearbyGuardians
				othersAdapter.items = viewModel.othersGuardians
			}, {
				isShowProgressBar(false)
			}, {
				isShowProgressBar()
			})
		})
		
		viewModel.getStreamsFromLocal().observe(viewLifecycleOwner, { streams ->
			setAlertFeatures(streams)
		})
		
		viewModel.getTrackingFromLocal().observe(viewLifecycleOwner, { trackings ->
			setTrackingFeatures(trackings)
		})
	}
	
	private fun setProjectTitle(str: String) {
		projectTitleTextView.text = str
	}
	
	override fun onLockImageClicked() {
		Toast.makeText(context, R.string.not_have_permission, Toast.LENGTH_LONG).show()
	}
	
	override fun invoke(guardian: EventGroup) {
		listener.openGuardianEventDetail(guardian.streamName, guardian.distance, guardian.eventSize, guardian.streamId)
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
	
	private fun isShowProgressBar(show: Boolean = true) {
		progressBar.visibility = if (show) View.VISIBLE else View.GONE
	}
	
	private fun isShowNotHaveStreams(show: Boolean) {
		notHaveStreamsGroupView.visibility = if (show) View.VISIBLE else View.GONE
	}
	
	private fun setShowListStream() {
		nearbyLayout.visibility = if (viewModel.nearbyGuardians.isNotEmpty()) View.VISIBLE else View.GONE
		othersLayout.visibility = if (viewModel.othersGuardians.isNotEmpty()) View.VISIBLE else View.GONE
		nearbyTextView.visibility = if (viewModel.nearbyGuardians.isNotEmpty() && viewModel.othersGuardians.isNotEmpty()) View.VISIBLE else View.GONE
		othersTextView.visibility = if (viewModel.nearbyGuardians.isNotEmpty() && viewModel.othersGuardians.isNotEmpty()) View.VISIBLE else View.GONE
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
			addLineLayer(style)
			alertFeatures?.let { moveCameraToLeavesBounds(it) }
		}
		
		mapboxMap.addOnMapClickListener { latLng ->
			handleClickIcon(mapboxMap.projection.toScreenLocation(latLng))
		}
	}
	
	private fun setAlertFeatures(streams: List<Stream>) {
		val projectId = preferences.getInt(Preferences.SELECTED_PROJECT, -1)
		
		val projectServerId = viewModel.getProject(projectId)?.serverId
		val listOfStream = streams.filter { s -> s.projectServerId == projectServerId }
		val features = listOfStream.map {
			val loc = Location(LocationManager.GPS_PROVIDER)
			loc.latitude = it.latitude
			loc.longitude = it.longitude
			
			val last = Location(LocationManager.GPS_PROVIDER)
			last.latitude = lastLocation?.latitude ?: 0.0
			last.longitude = lastLocation?.longitude ?: 0.0
			
			val properties = mapOf(
					Pair(PROPERTY_MARKER_ALERT_SITE, it.name),
					Pair(PROPERTY_MARKER_ALERT_COUNT, viewModel.getEventsCount(it.serverId)),
					Pair(PROPERTY_MARKER_ALERT_DISTANCE, viewModel.distance(last, loc)),
					Pair(PROPERTY_MARKER_ALERT_STREAM_ID, it.serverId)
			)
			Feature.fromGeometry(Point.fromLngLat(it.longitude, it.latitude), properties.toJsonObject())
		}
		alertFeatures = FeatureCollection.fromFeatures(features)
		refreshSource()
	}
	
	private fun setTrackingFeatures(trackingList: List<Tracking>) {
		trackingList.map { tracking ->
			val trackingCoordinates = tracking.points.map { p -> Point.fromLngLat(p.longitude, p.latitude) }
			lineFeatures = FeatureCollection.fromFeatures(arrayOf(Feature.fromGeometry(LineString.fromLngLats(trackingCoordinates))))
			refreshSource()
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
						val name = features[0].getProperty(PROPERTY_MARKER_ALERT_SITE).asString
						val distance = features[0].getProperty(PROPERTY_MARKER_ALERT_DISTANCE).asString
						val eventSize = features[0].getProperty(PROPERTY_MARKER_ALERT_COUNT).asString
						val streamId = features[0].getProperty(PROPERTY_MARKER_ALERT_STREAM_ID).asString
						listener.openGuardianEventDetail(name, distance.toDouble(), eventSize.toInt(), streamId)
					} else {
						moveCameraToLeavesBounds(clusterLeavesFeatureCollection)
					}
				}
			} else {
				val selectedFeature = alertFeatures[0]
				val name = selectedFeature.getProperty(PROPERTY_MARKER_ALERT_SITE).asString
				val distance = selectedFeature.getProperty(PROPERTY_MARKER_ALERT_DISTANCE).asString
				val eventSize = selectedFeature.getProperty(PROPERTY_MARKER_ALERT_COUNT).asString
				val streamId = selectedFeature.getProperty(PROPERTY_MARKER_ALERT_STREAM_ID).asString
				listener.openGuardianEventDetail(name, distance.toDouble(), eventSize.toInt(), streamId)
			}
			return true
		}
		return false
	}
	
	private fun setupSources(style: Style) {
		alertSource = GeoJsonSource(SOURCE_ALERT, FeatureCollection.fromFeatures(listOf()), GeoJsonOptions()
				.withCluster(true)
				.withClusterMaxZoom(15)
				.withClusterRadius(20)
				.withClusterProperty(
						PROPERTY_CLUSTER_TYPE,
						Expression.sum(Expression.accumulated(), Expression.get(PROPERTY_CLUSTER_TYPE)),
						Expression.switchCase(
								Expression.any(
										Expression.eq(
												Expression.get(PROPERTY_MARKER_ALERT_COUNT), "0"
										)
								),
								Expression.literal(0),
								Expression.literal(1)
						)
				).withClusterProperty(
						PROPERTY_CLUSTER_COUNT_EVENTS,
						Expression.sum(Expression.accumulated(), Expression.get(PROPERTY_CLUSTER_COUNT_EVENTS)),
						Expression.toNumber(Expression.get(PROPERTY_MARKER_ALERT_COUNT))
				)
		)
		style.addSource(alertSource!!)
		
		lineSource = GeoJsonSource(SOURCE_LINE)
		style.addSource(lineSource!!)
	}
	
	private fun refreshSource() {
		if (alertSource != null && alertFeatures != null) {
			alertSource!!.setGeoJson(alertFeatures)
		}
		if (lineSource != null && lineFeatures != null) {
			lineSource!!.setGeoJson(lineFeatures)
		}
	}
	
	private fun addLineLayer(style: Style) {
		val lineLayer = LineLayer("line-layer", SOURCE_LINE).withProperties(
				PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
				PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
				PropertyFactory.lineWidth(5f),
				PropertyFactory.lineColor(Color.parseColor("#e55e5e"))
		)
		style.addLayer(lineLayer)
	}
	
	private fun addClusteredGeoJsonSource(style: Style) {
		val layers = Array(2) { IntArray(2) }
		layers[0] = intArrayOf(0, Color.parseColor("#2FB04A"))
		layers[1] = intArrayOf(1, Color.parseColor("#e41a1a"))
		
		queryLayerIds = Array(layers.size) { "" }
		
		layers.forEachIndexed { index, layer ->
			queryLayerIds[index] = "cluster-$index"
			val circles = CircleLayer(queryLayerIds[index], SOURCE_ALERT)
			circles.setProperties(PropertyFactory.circleColor(layer[1]), PropertyFactory.circleRadius(14f))
			val type = Expression.toNumber(Expression.get(PROPERTY_CLUSTER_TYPE))
			circles.setFilter(
					if (index == 0) {
						Expression.gte(type, Expression.literal(layer[0]))
					} else {
						Expression.all(
								Expression.gte(type, Expression.literal(layer[0])),
								Expression.gt(type, Expression.literal(layers[index - 1][0]))
						)
					}
			)
			style.addLayer(circles)
		}
		
		val count = SymbolLayer(COUNT, SOURCE_ALERT)
		count.setProperties(
				PropertyFactory.textField(Expression.toString(Expression.get(PROPERTY_CLUSTER_COUNT_EVENTS))),
				PropertyFactory.textSize(12f),
				PropertyFactory.textColor(Color.WHITE),
				PropertyFactory.textIgnorePlacement(true),
				PropertyFactory.textAllowOverlap(true)
		)
		style.addLayer(count)
		
		layers.forEachIndexed { i, ly ->
			val unClustered = CircleLayer("UN_CLUSTERED_POINTS-$i", SOURCE_ALERT)
			val color = if (Expression.toString(Expression.get(PROPERTY_MARKER_ALERT_COUNT)).toString() != "0") Color.parseColor("#e41a1a") else Color.parseColor("#2FB04A")
			unClustered.setProperties(PropertyFactory.circleColor(color), PropertyFactory.circleRadius(14f))
			val eventsSize = Expression.toNumber(Expression.get(PROPERTY_MARKER_ALERT_COUNT))
			unClustered.setFilter(
					if (i == 0) {
						Expression.all(
								Expression.gte(eventsSize, Expression.literal(ly[0])),
								Expression.gte(eventsSize, Expression.literal(1))
						)
					} else {
						Expression.all(
								Expression.gte(eventsSize, Expression.literal(ly[0])),
								Expression.gt(eventsSize, Expression.literal(layers[i - 1][0]))
						)
					}
			)
			style.addLayer(unClustered)
		}
		
		val eventsSize = SymbolLayer(COUNT_EVENTS, SOURCE_ALERT)
		eventsSize.setProperties(
				PropertyFactory.textField(Expression.toString(Expression.get(PROPERTY_MARKER_ALERT_COUNT))),
				PropertyFactory.textSize(12f),
				PropertyFactory.textColor(Color.WHITE),
				PropertyFactory.textIgnorePlacement(true),
				PropertyFactory.textAllowOverlap(true)
		)
		style.addLayer(eventsSize)
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
		
		// Check if permissions are enabled and if not request
		if (PermissionsManager.areLocationPermissionsGranted(context)) {
			locationManager?.removeUpdates(locationListener)
			locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
			try {
				lastLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
				lastLocation?.let {
					moveCameraToCurrentLocation(it)
					viewModel.saveLastTimeToKnowTheCurrentLocation(requireContext(), Date().time)
				}
				mapBoxMap?.style?.let { enableLocationComponent(it) }
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
