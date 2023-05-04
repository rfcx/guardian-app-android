package org.rfcx.incidents.view.guardian.checklist.site

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.mapbox.android.core.location.*
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentGuardianSiteSetBinding
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.MapboxCameraUtils
import org.rfcx.incidents.util.latitudeCoordinates
import org.rfcx.incidents.util.longitudeCoordinates
import org.rfcx.incidents.util.setFormatLabel
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener

class GuardianSiteSetFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentGuardianSiteSetBinding
    private val viewModel: GuardianSiteSelectViewModel by viewModel()
    private var mainEvent: GuardianDeploymentEventListener? = null

    // Mapbox
    private var mapboxMap: MapboxMap? = null
    private lateinit var mapView: MapView
    private var symbolManager: SymbolManager? = null

    // Arguments
    var isUseCurrentLocate: Boolean = false
    lateinit var site: Stream
    var fromMapPicker: Boolean = false

    // Location
    private var currentUserLocation: Location? = null
    private var userLocation: Location? = null
    private lateinit var pinLocation: LatLng
    private var locationEngine: LocationEngine? = null
    private val mapboxLocationChangeCallback =
        object : LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(result: LocationEngineResult?) {
                if (activity != null) {
                    val location = result?.lastLocation
                    location ?: return
                    currentUserLocation = location
                    updateView()

                    if (isCreateNew && !fromMapPicker) {
                        currentUserLocation?.let { currentUserLocation ->
                            val latLng =
                                LatLng(currentUserLocation.latitude, currentUserLocation.longitude)
                            moveCamera(latLng, null, DefaultSetupMap.DEFAULT_ZOOM)
                        }
                    }
                    pinLocation?.let { setWithInText(location.toLatLng(), it) }
                }
            }

            override fun onFailure(exception: Exception) {}
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let { Mapbox.getInstance(it, getString(R.string.mapbox_token)) }
        initIntent()
    }

    private fun initIntent() {
        arguments?.let {
            site = it.getSerializable(ARG_SITE) as Stream
            fromMapPicker = it.getBoolean(ARG_FROM_MAP_PICKER)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainEvent = context as GuardianDeploymentEventListener
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guardian_site_set, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainEvent?.let {
            it.showToolbar()
            it.setToolbarTitle("Installation site selection")
        }

        mapView = view.findViewById(R.id.mapBoxView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        binding.nextButton.setOnClickListener {
            if (id == -1) {
                createSite()
            } else {
                handleExistLocate()
            }
        }

        binding.currentLocate.setOnClickListener {
            //TODO: viewmodel to use current location
        }

        binding.viewMapBox.setOnClickListener {
            //TODO: go to map picker
        }
    }

    private fun createSite() {
        //TODO: viewmodel for create site
    }

    private fun handleExistLocate() {
        //TODO: viewmodel for update site
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.uiSettings.setAllGesturesEnabled(false)
        mapboxMap.uiSettings.isAttributionEnabled = false
        mapboxMap.uiSettings.isLogoEnabled = false
        mapboxMap.setStyle(Style.OUTDOORS) {
            setupSymbolManager(it)
            setPinOnMap()
            enableLocationComponent()
        }
    }

    private fun setPinOnMap() {
        val curLoc = LatLng()
        val siteLoc = LatLng(site.latitude, site.longitude)
        moveCamera(curLoc, siteLoc, DEFAULT_ZOOM)
        createSiteSymbol(siteLoc)
        pinLocation = siteLoc
    }

    private fun setupSymbolManager(style: Style) {
        this.mapboxMap?.let { mapboxMap ->
            symbolManager = SymbolManager(this.mapView, mapboxMap, style)
            symbolManager?.iconAllowOverlap = true

            style.addImage(
                PROPERTY_MARKER_IMAGE,
                ResourcesCompat.getDrawable(this.resources, R.drawable.ic_pin_map, null)!!
            )
        }
    }

    private fun createSiteSymbol(latLng: LatLng) {
        symbolManager?.deleteAll()
        symbolManager?.create(
            SymbolOptions()
                .withLatLng(latLng)
                .withIconImage(PROPERTY_MARKER_IMAGE)
                .withIconSize(0.75f)
        )
    }

    private fun hasPermissions(): Boolean {
        val permissionState = context?.let {
            ActivityCompat.checkSelfPermission(
                it,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent() {
        if (hasPermissions()) {
            val loadedMapStyle = mapboxMap?.style
            val locationComponent = mapboxMap?.locationComponent
            // Activate the LocationComponent
            val customLocationComponentOptions = context?.let {
                LocationComponentOptions.builder(it)
                    .trackingGesturesManagement(true)
                    .accuracyColor(ContextCompat.getColor(it, R.color.colorPrimary))
                    .build()
            }

            val locationComponentActivationOptions =
                context?.let {
                    LocationComponentActivationOptions.builder(it, loadedMapStyle!!)
                        .locationComponentOptions(customLocationComponentOptions)
                        .build()
                }

            mapboxMap?.let { it ->
                it.locationComponent.apply {
                    if (locationComponentActivationOptions != null) {
                        activateLocationComponent(locationComponentActivationOptions)
                    }

                    isLocationComponentEnabled = true
                    renderMode = RenderMode.COMPASS
                }
            }

            this.currentUserLocation = locationComponent?.lastKnownLocation
            initLocationEngine()
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity?.requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        } else {
            throw Exception("Request permissions not required before API 23 (should never happen)")
        }
    }

    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        locationEngine = context?.let { LocationEngineProvider.getBestLocationEngine(it) }
        val request =
            LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build()
        locationEngine?.requestLocationUpdates(
            request,
            mapboxLocationChangeCallback,
            Looper.getMainLooper()
        )
        locationEngine?.getLastLocation(mapboxLocationChangeCallback)
    }

    private fun moveCamera(userPosition: LatLng, nearestSite: LatLng?, zoom: Double) {
        mapboxMap?.moveCamera(
            MapboxCameraUtils.calculateLatLngForZoom(
                userPosition,
                nearestSite,
                zoom
            )
        )
    }

    private fun moveCamera(latLng: LatLng, zoom: Double) {
        mapboxMap?.moveCamera(MapboxCameraUtils.calculateLatLngForZoom(latLng, null, zoom))
    }

    private fun setWithInText(curLoc: LatLng, target: LatLng) {
        val distance = curLoc.distanceTo(target)
        if (distance <= 20) {
            setWithinText()
        } else {
            setNotWithinText(distance.setFormatLabel())
        }
    }

    private fun setWithinText() {
        binding.withinTextView.text = "within"
        binding.withinTextView.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_checklist_passed,
            0,
            0,
            0
        )
    }

    private fun setNotWithinText(distance: String) {
        binding.withinTextView.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_checklist_cross,
            0,
            0,
            0
        )
        binding.withinTextView.text = getString(R.string.more_than, distance)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    companion object {
        const val DEFAULT_ZOOM = 15.0
        const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
        const val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
        const val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5

        private const val ARG_SITE = "ARG_SITE"
        private const val ARG_FROM_MAP_PICKER = "ARG_FROM_MAP_PICKER"

        const val PROPERTY_MARKER_IMAGE = "marker.image"

        @JvmStatic
        fun newInstance() = GuardianSiteSetFragment()

        fun newInstance(stream: Stream) =
            GuardianSiteSetFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_SITE, stream)
                }
            }

        fun newInstance(stream: Stream, fromMapPicker: Boolean = false) =
            GuardianSiteSetFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_SITE, stream)
                    putBoolean(ARG_FROM_MAP_PICKER, fromMapPicker)
                }
            }
    }
}
