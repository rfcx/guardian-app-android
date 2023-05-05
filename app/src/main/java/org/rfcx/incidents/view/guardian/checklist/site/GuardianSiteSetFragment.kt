package org.rfcx.incidents.view.guardian.checklist.site

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.incidents.R
import org.rfcx.incidents.databinding.FragmentGuardianSiteSetBinding
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.MapboxCameraUtils
import org.rfcx.incidents.util.setFormatLabel
import org.rfcx.incidents.view.guardian.GuardianDeploymentEventListener

class GuardianSiteSetFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentGuardianSiteSetBinding
    private val viewModel: GuardianSiteSetViewModel by viewModel()
    private var mainEvent: GuardianDeploymentEventListener? = null

    // Mapbox
    private var mapboxMap: MapboxMap? = null
    private lateinit var mapView: MapView
    private var symbolManager: SymbolManager? = null

    // Arguments
    private lateinit var site: Stream
    var fromMapPicker: Boolean = false
    private var isNewSite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let { Mapbox.getInstance(it, getString(R.string.mapbox_token)) }
        initIntent()
    }

    private fun initIntent() {
        arguments?.let {
            site = it.getSerializable(ARG_SITE) as Stream
            fromMapPicker = it.getBoolean(ARG_FROM_MAP_PICKER)
            isNewSite = it.getBoolean(ARG_IS_NEW_SITE)
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

        binding.viewModel = viewModel

        mainEvent?.let {
            it.showToolbar()
            it.setToolbarTitle(getString(R.string.guardian_site_title))
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
            viewModel.updateSiteToCurrentLocation()
            setPinOnMap()
        }

        binding.viewMapBox.setOnClickListener {
            //TODO: go to map picker
        }

        viewModel.setSite(site)
        viewModel.setIsNewSite(isNewSite)
        collectCurrentLoc()
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
        val curLoc = LatLng(viewModel.currentLocationState.value?.latitude ?: 0.0, viewModel.currentLocationState.value?.longitude ?: 0.0)
        val siteLoc = LatLng(site.latitude, site.longitude)
        moveCamera(curLoc, siteLoc, DEFAULT_ZOOM)
        createSiteSymbol(siteLoc)
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

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent() {
        val loadedMapStyle = mapboxMap?.style
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

    private fun collectCurrentLoc() {
        lifecycleScope.launch {
            viewModel.currentLocationState.collectLatest { currentLoc ->
                currentLoc?.let {
                    val curLoc = LatLng(it.latitude, it.longitude)
                    val siteLoc = LatLng(site.latitude, site.longitude)
                    setWithInText(curLoc, siteLoc)
                }
            }
        }
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
        binding.withinTextView.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_checklist_passed,
            0,
            0,
            0
        )
        binding.withinTextView.text = getString(R.string.within)

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

        private const val ARG_SITE = "ARG_SITE"
        private const val ARG_FROM_MAP_PICKER = "ARG_FROM_MAP_PICKER"
        private const val ARG_IS_NEW_SITE = "ARG_IS_NEW_SITE"

        const val PROPERTY_MARKER_IMAGE = "marker.image"

        @JvmStatic
        fun newInstance() = GuardianSiteSetFragment()

        fun newInstance(stream: Stream, isNewSite: Boolean = false) =
            GuardianSiteSetFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_SITE, stream)
                    putBoolean(ARG_IS_NEW_SITE, isNewSite)
                }
            }

        fun newInstance(stream: Stream, fromMapPicker: Boolean = false, isNewSite: Boolean = false) =
            GuardianSiteSetFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_SITE, stream)
                    putBoolean(ARG_FROM_MAP_PICKER, fromMapPicker)
                    putBoolean(ARG_IS_NEW_SITE, isNewSite)
                }
            }
    }
}
