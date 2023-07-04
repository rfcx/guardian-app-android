package org.rfcx.incidents.view.events

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.common.success
import org.rfcx.incidents.databinding.FragmentStreamsBinding
import org.rfcx.incidents.entity.CrashlyticsKey
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.entity.stream.Project
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.service.EventNotification
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.util.Crashlytics
import org.rfcx.incidents.util.Screen
import org.rfcx.incidents.util.isNetworkAvailable
import org.rfcx.incidents.util.isOnAirplaneMode
import org.rfcx.incidents.util.toJsonObject
import org.rfcx.incidents.view.MainActivityEventListener
import org.rfcx.incidents.view.events.adapter.ProjectAdapter
import org.rfcx.incidents.view.events.adapter.ProjectOnClickListener
import org.rfcx.incidents.view.events.adapter.StreamAdapter
import java.util.Date

class StreamsFragment :
    Fragment(),
    OnMapReadyCallback,
    PermissionsListener,
    ProjectOnClickListener,
    SwipeRefreshLayout.OnRefreshListener,
        (Stream) -> Unit {

    companion object {
        const val tag = "EventsFragment"

        private const val COUNT = "count"
        private const val COUNT_EVENTS = "count.events"
        private const val POINT_COUNT = "point_count"
        private const val SOURCE_EVENT = "source.event"
        private const val SOURCE_LINE = "source.line"
        private const val PROPERTY_MARKER_EVENT_SITE = "event.site"
        private const val PROPERTY_MARKER_EVENT_DISTANCE = "event.distance"
        private const val PROPERTY_MARKER_EVENT_STREAM_ID = "event.stream.id"
        private const val PROPERTY_MARKER_EVENT_COUNT = "event.count"
        private const val PROPERTY_CLUSTER_TYPE = "cluster.type"
        private const val PROPERTY_CLUSTER_COUNT_EVENTS = "cluster.count.events"
        private const val SOURCE_CHECK_IN = "source.checkin"
        private const val MARKER_CHECK_IN_IMAGE = "marker.checkin.pin"
        private const val MARKER_CHECK_IN_ID = "marker.checkin"

        private const val DEFAULT_MAP_ZOOM = 15.0
        private const val PADDING_BOUNDS = 230
        private const val DURATION_MS = 1300
        private const val THREE_HOURS = 3 * 60 * 60 * 1000

        @JvmStatic
        fun newInstance() = StreamsFragment()
    }

    private var _binding: FragmentStreamsBinding? = null
    private val binding get() = _binding!!

    private val analytics by lazy { context?.let { Analytics(it) } }
    private val firebaseCrashlytics by lazy { Crashlytics() }

    private val viewModel: StreamsViewModel by viewModel()
    private val projectAdapter by lazy { ProjectAdapter(this) }
    private val streamAdapter by lazy { StreamAdapter(this) }
    lateinit var preferences: Preferences

    private lateinit var mapView: MapView
    private var mapBoxMap: MapboxMap? = null
    private var locationManager: LocationManager? = null
    private var lastLocation: Location? = null
    private var permissionsManager: PermissionsManager = PermissionsManager(this)
    private val locationListener = object : android.location.LocationListener {
        override fun onLocationChanged(p0: Location) {
            moveCameraToCurrentLocation(p0)
            viewModel.saveLastTimeToKnowTheCurrentLocation(Date().time)

            if (PermissionsManager.areLocationPermissionsGranted(context)) {
                mapBoxMap?.style?.let { style -> enableLocationComponent(style) }
            }
        }

        override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
        override fun onProviderEnabled(p0: String) {}
        override fun onProviderDisabled(p0: String) {}
    }

    private var eventSource: GeoJsonSource? = null
    private var eventFeatures: FeatureCollection? = null

    private var lineSource: GeoJsonSource? = null
    private var lineFeatures: FeatureCollection? = null

    private var checkInSource: GeoJsonSource? = null
    private var checkInFeatures: FeatureCollection? = null

    private var queryLayerIds: Array<String> = arrayOf()
    private var isShowMapIcon = true
    lateinit var listener: MainActivityEventListener
    private lateinit var localBroadcastManager: LocalBroadcastManager

    private val streamIdReceived = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            val streamId = intent.getStringExtra(EventNotification.INTENT_KEY_STREAM_ID)
            if (streamId != null) {
                viewModel.refreshStreams(force = true)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (context as MainActivityEventListener)
        localBroadcastManager = LocalBroadcastManager.getInstance(context)
        val actionReceiver = IntentFilter()
        actionReceiver.addAction("haveNewEvent")
        localBroadcastManager.registerReceiver(streamIdReceived, actionReceiver)
    }

    override fun onDetach() {
        super.onDetach()
        localBroadcastManager.unregisterReceiver(streamIdReceived)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let { Mapbox.getInstance(it, getString(R.string.mapbox_token)) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStreamsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        preferences = Preferences.getInstance(requireContext())

        // Show loading indicator for first time
        isShowProgressBar()

        getLocation()
        setupToolbar()
        setOnClickListener()
        setObserver()
        setRecyclerView()
        onClickCurrentLocationButton()

        binding.refreshView.apply {
            setOnRefreshListener(this@StreamsFragment)
            setColorSchemeResources(R.color.colorPrimary)
        }

        viewModel.refreshProjects()
    }

    private fun onClickCurrentLocationButton() {
        binding.currentLocationButton.setOnClickListener {
            mapBoxMap?.locationComponent?.isLocationComponentActivated?.let {
                if (it) {
                    moveCameraToCurrentLocation()
                } else {
                    getLocation()
                }
            }
        }
    }

    private fun setRecyclerView() {
        binding.projectRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = projectAdapter
        }

        val streamsLayoutManager = LinearLayoutManager(context)
        binding.streamRecyclerView.apply {
            layoutManager = streamsLayoutManager
            adapter = streamAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val visibleItemCount = streamsLayoutManager.childCount
                    val total = streamsLayoutManager.itemCount
                    val firstVisibleItemPosition = streamsLayoutManager.findFirstVisibleItemPosition()
                    if (!binding.refreshView.isRefreshing &&
                        (visibleItemCount + firstVisibleItemPosition) >= total &&
                        firstVisibleItemPosition >= 0 && !viewModel.isLoadingMore
                    ) {
                        viewModel.refreshStreams(force = true, total)
                    }
                }
            })
        }
    }

    private fun setOnClickListener() {
        binding.toolbarLayout.projectTitleLayout.setOnClickListener {
            projectNameSelected()
        }

        binding.projectSwipeRefreshView.apply {
            setOnRefreshListener {
                isRefreshing = true
                when {
                    requireContext().isOnAirplaneMode() || !requireContext().isNetworkAvailable() -> {
                        isRefreshing = false
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.project_could_not_refreshed) + " " + getString(R.string.pls_off_air_plane_mode),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        viewModel.refreshProjects(true)
                    }
                }
            }
            setColorSchemeResources(R.color.colorPrimary)
        }
    }

    private fun projectNameSelected() {
        if (binding.projectRecyclerView.visibility == View.VISIBLE) {
            binding.toolbarLayout.expandMoreImageView.rotation = 0F
            listener.showBottomAppBar()
            binding.projectRecyclerView.visibility = View.GONE
            binding.projectSwipeRefreshView.visibility = View.GONE
        } else {
            binding.toolbarLayout.expandMoreImageView.rotation = 180F
            listener.hideBottomAppBar()
            binding.projectRecyclerView.visibility = View.VISIBLE
            binding.projectSwipeRefreshView.visibility = View.VISIBLE
        }
    }

    override fun onProjectClicked(project: Project) {
        viewModel.selectProject(project.id)
        firebaseCrashlytics.setCustomKey(CrashlyticsKey.OnSelectedProject.key, project.id + " : " + project.name)

        isShowNotHaveStreams(false)
        binding.streamLayout.visibility = View.GONE
        binding.toolbarLayout.expandMoreImageView.rotation = 0F

        isShowProgressBar()

        listener.showBottomAppBar()
        binding.projectRecyclerView.visibility = View.GONE
        binding.projectSwipeRefreshView.visibility = View.GONE

        when {
            requireContext().isOnAirplaneMode() -> {
                Toast.makeText(requireContext(), getString(R.string.pls_off_air_plane_mode), Toast.LENGTH_LONG).show()
            }
            !requireContext().isNetworkAvailable() -> {
                Toast.makeText(requireContext(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setObserver() {

        viewModel.selectedProject.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> binding.toolbarLayout.projectTitleTextView.text = result.data.name
                else -> binding.toolbarLayout.projectTitleTextView.text = ""
            }
        }

        viewModel.projects.observe(viewLifecycleOwner) { result ->
            result.success({ projects ->
                binding.projectSwipeRefreshView.isRefreshing = false
                projectAdapter.items = projects
                projectAdapter.notifyDataSetChanged()
            }, {
                binding.projectSwipeRefreshView.isRefreshing = false
                Toast.makeText(
                    context,
                    it.message
                        ?: getString(R.string.something_is_wrong),
                    Toast.LENGTH_LONG
                ).show()
            }, {
            })
        }

        viewModel.streams.observe(viewLifecycleOwner) { it ->
            it.success({ streams ->
                streamAdapter.items = streams.filter { it.lastIncident != null }
                streamAdapter.notifyDataSetChanged()
                setEventFeatures(streams)
                binding.streamLayout.visibility = View.VISIBLE
                binding.refreshView.isRefreshing = false
                isShowProgressBar(false)
                isShowNotHaveStreams(streams.isEmpty() && binding.mapView.visibility == View.GONE && binding.progressBar.visibility == View.GONE)
            }, {
                binding.refreshView.isRefreshing = false
                isShowProgressBar(false)
            }, {
                binding.refreshView.isRefreshing = false
                binding.streamLayout.visibility = View.GONE
                isShowProgressBar()
            })
        }

        viewModel.getTrackingFromLocal().observe(viewLifecycleOwner) { trackings ->
            setTrackingFeatures(trackings)
        }
    }

    override fun onLockImageClicked() {
        Toast.makeText(context, R.string.not_have_permission, Toast.LENGTH_LONG).show()
    }

    override fun invoke(stream: Stream) {
        firebaseCrashlytics.setCustomKey(CrashlyticsKey.OnClickStreamNewEventPage.key, "Stream name: " + stream.name + "/ Project id: " + stream.projectId)
        listener.openStreamDetail(stream.externalId ?: "", null)
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbarLayout.toolbar)

        binding.toolbarLayout.changePageImageView.setOnClickListener {
            if (isShowMapIcon) {
                analytics?.trackScreen(Screen.MAP)
                isShowNotHaveStreams(false)
                binding.toolbarLayout.changePageImageView.setImageResource(R.drawable.ic_view_list)
                mapView.visibility = View.VISIBLE
                binding.refreshView.visibility = View.GONE
                binding.currentLocationButton.visibility = View.VISIBLE
                mapBoxMap?.style?.let { style -> enableLocationComponent(style) }
            } else {
                binding.toolbarLayout.changePageImageView.setImageResource(R.drawable.ic_map)
                mapView.visibility = View.GONE
                binding.refreshView.visibility = View.VISIBLE
                binding.currentLocationButton.visibility = View.GONE
            }
            isShowMapIcon = !isShowMapIcon
        }
    }

    private fun isShowProgressBar(show: Boolean = true) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun isShowNotHaveStreams(show: Boolean) {
        binding.notHaveStreamsGroupView.visibility = if (show) View.VISIBLE else View.GONE
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
            eventFeatures?.let { moveCameraToLeavesBounds(it) }
        }

        mapboxMap.addOnMapClickListener { latLng ->
            handleClickIcon(mapboxMap.projection.toScreenLocation(latLng))
        }
    }

    private fun setEventFeatures(streams: List<Stream>) {
        val features = streams.map {
            val properties = mapOf(
                Pair(PROPERTY_MARKER_EVENT_SITE, it.name),
                Pair(PROPERTY_MARKER_EVENT_COUNT, (it.lastIncident?.events?.size ?: 0).toString()),
                Pair(PROPERTY_MARKER_EVENT_DISTANCE, distanceLabel(lastLocation, it)),
                Pair(PROPERTY_MARKER_EVENT_STREAM_ID, it.externalId.toString())
            )
            Feature.fromGeometry(Point.fromLngLat(it.longitude, it.latitude), properties.toJsonObject())
        }
        eventFeatures = FeatureCollection.fromFeatures(features)
        eventFeatures?.let { moveCameraToLeavesBounds(it) }
        refreshSource()
    }

    private fun setTrackingFeatures(trackingList: List<Tracking>) {
        trackingList.map { tracking ->
            val tracks = tracking.points.filter { t -> System.currentTimeMillis() - t.createdAt.time <= THREE_HOURS }
            val trackingCoordinates = tracks.map { p -> Point.fromLngLat(p.longitude, p.latitude) }
            lineFeatures =
                FeatureCollection.fromFeatures(arrayOf(Feature.fromGeometry(LineString.fromLngLats(trackingCoordinates))))

            // Create point
            val pointFeatures = tracks.map {
                Feature.fromGeometry(Point.fromLngLat(it.longitude, it.latitude))
            }

            checkInFeatures = FeatureCollection.fromFeatures(pointFeatures)
            refreshSource()
        }
    }

    private fun handleClickIcon(screenPoint: PointF): Boolean {
        val rectF = RectF(screenPoint.x - 10, screenPoint.y - 10, screenPoint.x + 10, screenPoint.y + 10)
        var eventFeatures = listOf<Feature>()
        queryLayerIds.forEach {
            val features = mapBoxMap?.queryRenderedFeatures(rectF, it) ?: listOf()
            if (features.isNotEmpty()) {
                eventFeatures = features
            }
        }

        if (eventFeatures.isNotEmpty()) {
            val pinCount =
                if (eventFeatures[0].getProperty(POINT_COUNT) != null) eventFeatures[0].getProperty(POINT_COUNT).asInt else 0
            if (pinCount > 1) {
                val clusterLeavesFeatureCollection = eventSource?.getClusterLeaves(eventFeatures[0], 8000, 0)
                val features = clusterLeavesFeatureCollection?.features()
                if (clusterLeavesFeatureCollection != null) {
                    if (features?.groupBy { it }?.size == 1) {
                        val distance = features[0].getProperty(PROPERTY_MARKER_EVENT_DISTANCE).asString
                        val streamId = features[0].getProperty(PROPERTY_MARKER_EVENT_STREAM_ID).asString
                        val streamName = features[0].getProperty(PROPERTY_MARKER_EVENT_SITE).asString
                        firebaseCrashlytics.setCustomKey(CrashlyticsKey.OnClickStreamMapPage.key, streamName)
                        listener.openStreamDetail(streamId, distance.toDouble())
                    } else {
                        moveCameraToLeavesBounds(clusterLeavesFeatureCollection)
                    }
                }
            } else {
                val selectedFeature = eventFeatures[0]
                val distance = selectedFeature.getProperty(PROPERTY_MARKER_EVENT_DISTANCE).asString
                val streamId = selectedFeature.getProperty(PROPERTY_MARKER_EVENT_STREAM_ID).asString
                val streamName = selectedFeature.getProperty(PROPERTY_MARKER_EVENT_SITE).asString
                firebaseCrashlytics.setCustomKey(CrashlyticsKey.OnClickStreamMapPage.key, streamName)
                listener.openStreamDetail(streamId, if (distance.isBlank()) null else distance.toDouble())
            }
            return true
        }
        return false
    }

    private fun setupSources(style: Style) {
        eventSource = GeoJsonSource(
            SOURCE_EVENT, FeatureCollection.fromFeatures(listOf()),
            GeoJsonOptions()
                .withCluster(true)
                .withClusterMaxZoom(15)
                .withClusterRadius(20)
                .withClusterProperty(
                    PROPERTY_CLUSTER_TYPE,
                    Expression.sum(Expression.accumulated(), Expression.get(PROPERTY_CLUSTER_TYPE)),
                    Expression.switchCase(
                        Expression.any(
                            Expression.eq(
                                Expression.get(PROPERTY_MARKER_EVENT_COUNT), "0"
                            )
                        ),
                        Expression.literal(0),
                        Expression.literal(1)
                    )
                ).withClusterProperty(
                    PROPERTY_CLUSTER_COUNT_EVENTS,
                    Expression.sum(Expression.accumulated(), Expression.get(PROPERTY_CLUSTER_COUNT_EVENTS)),
                    Expression.toNumber(Expression.get(PROPERTY_MARKER_EVENT_COUNT))
                )
        )
        style.addSource(eventSource!!)

        lineSource = GeoJsonSource(SOURCE_LINE)
        style.addSource(lineSource!!)

        checkInSource = GeoJsonSource(SOURCE_CHECK_IN, FeatureCollection.fromFeatures(listOf()))
        style.addSource(checkInSource!!)
    }

    private fun refreshSource() {
        if (eventSource != null && eventFeatures != null) {
            eventSource!!.setGeoJson(eventFeatures)
        }
        if (lineSource != null && lineFeatures != null) {
            lineSource!!.setGeoJson(lineFeatures)
        }
        if (checkInSource != null && checkInFeatures != null) {
            checkInSource!!.setGeoJson(checkInFeatures)
        }
    }

    private fun addLineLayer(style: Style) {
        val lineLayer = LineLayer("line-layer", SOURCE_LINE).withProperties(
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
            PropertyFactory.lineWidth(5f),
            PropertyFactory.lineColor(resources.getColor(R.color.tracking_line))
        )
        style.addLayer(lineLayer)

        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.bg_circle_tracking, null)
        val mBitmap = BitmapUtils.getBitmapFromDrawable(drawable)
        if (mBitmap != null) {
            style.addImage(MARKER_CHECK_IN_IMAGE, mBitmap)
        }

        val checkInLayer = SymbolLayer(MARKER_CHECK_IN_ID, SOURCE_CHECK_IN).apply {
            withProperties(
                PropertyFactory.iconImage(MARKER_CHECK_IN_IMAGE),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.iconSize(1f)
            )
        }
        style.addLayer(checkInLayer)
    }

    private fun addClusteredGeoJsonSource(style: Style) {
        val layers = Array(2) { IntArray(2) }
        layers[0] = intArrayOf(0, Color.parseColor("#2FB04A"))
        layers[1] = intArrayOf(1, Color.parseColor("#e41a1a"))

        queryLayerIds = Array(layers.size) { "" }

        layers.forEachIndexed { index, layer ->
            queryLayerIds[index] = "cluster-$index"
            val circles = CircleLayer(queryLayerIds[index], SOURCE_EVENT)
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

        val count = SymbolLayer(COUNT, SOURCE_EVENT)
        count.setProperties(
            PropertyFactory.textField(Expression.toString(Expression.get(PROPERTY_CLUSTER_COUNT_EVENTS))),
            PropertyFactory.textSize(12f),
            PropertyFactory.textColor(Color.WHITE),
            PropertyFactory.textIgnorePlacement(true),
            PropertyFactory.textAllowOverlap(true)
        )
        style.addLayer(count)

        layers.forEachIndexed { i, ly ->
            val unClustered = CircleLayer("UN_CLUSTERED_POINTS-$i", SOURCE_EVENT)
            val color = if (Expression.toString(Expression.get(PROPERTY_MARKER_EVENT_COUNT)).toString() != "0")
                resources.getColor(R.color.text_error) else resources.getColor(R.color.text_green)
            unClustered.setProperties(PropertyFactory.circleColor(color), PropertyFactory.circleRadius(14f))
            val eventsSize = Expression.toNumber(Expression.get(PROPERTY_MARKER_EVENT_COUNT))
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

        val eventsSize = SymbolLayer(COUNT_EVENTS, SOURCE_EVENT)
        eventsSize.setProperties(
            PropertyFactory.textField(Expression.toString(Expression.get(PROPERTY_MARKER_EVENT_COUNT))),
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
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                isLocationComponentEnabled = true

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
                    listener.setCurrentLocation(it)
                    moveCameraToCurrentLocation(it)
                    viewModel.saveLastTimeToKnowTheCurrentLocation(Date().time)
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

    private fun moveCameraTo(latLng: LatLng, zoom: Double = DEFAULT_MAP_ZOOM) {
        mapBoxMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
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

    private fun moveCameraToCurrentLocation() {
        mapBoxMap?.locationComponent?.lastKnownLocation?.let { curLoc ->
            val currentLatLng = LatLng(curLoc.latitude, curLoc.longitude)
            moveCameraTo(currentLatLng, mapBoxMap?.cameraPosition?.zoom ?: DEFAULT_MAP_ZOOM)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.refreshStreams()

            val projectId = preferences.getString(Preferences.SELECTED_PROJECT)
            projectId?.let { viewModel.selectProject(it) }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        analytics?.trackScreen(Screen.NEW_EVENTS)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()

        // if (!context.isNetworkAvailable()) {
        //     isShowProgressBar(false)
        // } else {
        //     viewModel.refreshStreams()
        // }
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

    override fun onRefresh() {
        if (context.isNetworkAvailable()) {
            viewModel.fetchFreshStreams(force = true)
        } else {
            binding.refreshView.isRefreshing = false
            Toast.makeText(requireContext(), getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show()
        }
    }
}

fun distanceLabel(origin: Location?, destination: Stream): String {
    if (origin == null) return ""
    return LatLng(origin.latitude, origin.longitude).distanceTo(LatLng(destination.latitude, destination.longitude)).toString()
}
