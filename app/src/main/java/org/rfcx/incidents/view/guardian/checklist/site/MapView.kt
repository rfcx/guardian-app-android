package org.rfcx.incidents.view.guardian.checklist.site

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
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
import org.rfcx.incidents.util.MapboxCameraUtils

class MapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MapView(context, attrs, defStyleAttr), OnMapReadyCallback {

    companion object {
        private const val PROPERTY_MARKER_IMAGE = "marker.image"
        private const val DEFAULT_ZOOM = 15.0
    }

    private lateinit var mapbox: MapboxMap
    private lateinit var style: Style
    private lateinit var symbolManager: SymbolManager

    private var currentLoc = LatLng()
    private var siteLoc = LatLng()
    private var canMove = false
    private var createPinAtCurrentLoc = false
    private lateinit var callback: (LatLng) -> Unit

    init {
        initAttrs(attrs)
        setupView()
        this.setOnTouchListener { v, event -> true }
    }

    private fun initAttrs(attrs: AttributeSet?) {
        if (attrs == null) return
    }

    private fun setupView() {
        getMapAsync(this)
    }

    fun setParam(canMove: Boolean = true, createPinAtCurrentLoc: Boolean = false) {
        this.canMove = canMove
        this.createPinAtCurrentLoc = createPinAtCurrentLoc
    }

    fun setCameraMoveCallback(callback: (LatLng) -> Unit) {
        this.callback = callback
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        mapbox = mapboxMap
        mapboxMap.uiSettings.setAllGesturesEnabled(canMove)
        mapboxMap.uiSettings.isAttributionEnabled = false
        mapboxMap.uiSettings.isLogoEnabled = false
        mapboxMap.setStyle(Style.OUTDOORS) {
            style = it
            setupSymbolManager()
            enableLocationComponent()
            if (currentLoc.latitude == 0.0 && currentLoc.longitude == 0.0) {
                moveCamera(siteLoc)
            } else {
                if (siteLoc.latitude == 0.0 && siteLoc.longitude == 0.0) {
                    moveCamera(currentLoc)
                } else {
                    moveCamera(currentLoc, siteLoc)
                }
            }
            if (createPinAtCurrentLoc) {
                setPinOnMap(currentLoc, siteLoc)
            }
        }

        mapbox.addOnCameraMoveListener {
            val currentCameraPosition = mapbox.cameraPosition.target
            if (::callback.isInitialized) {
                this.callback.invoke(LatLng(currentCameraPosition.latitude, currentCameraPosition.longitude))
            }
        }
    }

    fun setCurrentLocation(currentLoc: LatLng) {
        this.currentLoc = currentLoc
    }

    fun setSiteLocation(siteLoc: LatLng) {
        this.siteLoc = siteLoc
    }

    private fun setupSymbolManager() {
        symbolManager = SymbolManager(this, mapbox, style)
        symbolManager.iconAllowOverlap = true
        style.addImage(
            PROPERTY_MARKER_IMAGE,
            ResourcesCompat.getDrawable(this.resources, R.drawable.ic_pin_map, null)!!
        )
    }

    fun setPinOnMap(currentLoc: LatLng, pinLoc: LatLng) {
        moveCamera(currentLoc, pinLoc)
        createSiteSymbol(pinLoc)
    }

    fun setPinOnMap(pinLoc: LatLng) {
        createSiteSymbol(pinLoc)
    }

    private fun createSiteSymbol(latLng: LatLng) {
        symbolManager.deleteAll()
        symbolManager.create(
            SymbolOptions()
                .withLatLng(latLng)
                .withIconImage(PROPERTY_MARKER_IMAGE)
                .withIconSize(0.75f)
        )
    }

    private fun moveCamera(userPosition: LatLng, nearestSite: LatLng?) {
        mapbox.moveCamera(
            MapboxCameraUtils.calculateLatLngForZoom(
                userPosition,
                nearestSite,
                DEFAULT_ZOOM
            )
        )
    }

    fun moveCamera(latLng: LatLng) {
        mapbox.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
    }

    fun getCurrentPosition(): LatLng {
        return mapbox.cameraPosition.target
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent() {
        val loadedMapStyle = mapbox.style
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

        mapbox.locationComponent.apply {
            if (locationComponentActivationOptions != null) {
                activateLocationComponent(locationComponentActivationOptions)
            }

            isLocationComponentEnabled = true
            renderMode = RenderMode.COMPASS
        }
    }
}
