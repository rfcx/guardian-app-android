package org.rfcx.incidents.view.guardian.checklist.site

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
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
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.expressions.Expression.eq
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import org.rfcx.incidents.R
import org.rfcx.incidents.util.MapboxCameraUtils
import org.rfcx.incidents.util.toJsonObject
import org.rfcx.incidents.util.toStringWithTimeZone
import org.rfcx.incidents.view.report.deployment.MapMarker
import java.util.TimeZone

class MapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MapView(context, attrs, defStyleAttr), OnMapReadyCallback {

    companion object {
        private const val PROPERTY_MARKER_IMAGE = "marker.image"
        private const val DEFAULT_ZOOM = 15.0

        private const val SITE_MARKER = "SITE_MARKER"

        private const val SOURCE_DEPLOYMENT = "source.deployment"
        private const val MARKER_DEPLOYMENT_ID = "marker.deployment"
        private const val MARKER_SITE_ID = "marker.site"

        private const val SOURCE_LINE = "source.line"

        private const val PROPERTY_DEPLOYMENT_SELECTED = "deployment.selected"
        private const val PROPERTY_DEPLOYMENT_MARKER_LOCATION_ID = "deployment.location"
        private const val PROPERTY_DEPLOYMENT_MARKER_TITLE = "deployment.title"
        private const val PROPERTY_DEPLOYMENT_MARKER_DEPLOYMENT_ID = "deployment.deployment"
        private const val PROPERTY_DEPLOYMENT_MARKER_IMAGE = "deployment.marker.image"
        private const val WINDOW_MARKER_ID = "info.marker"
        private const val PROPERTY_WINDOW_INFO_ID = "window.info.id"

        private const val PROPERTY_SITE_MARKER_IMAGE = "site.marker.image"
        private const val PROPERTY_SITE_MARKER_ID = "site.id"
        private const val PROPERTY_SITE_MARKER_SITE_ID = "site.stream.id"
        private const val PROPERTY_SITE_MARKER_SITE_NAME = "site.stream.name"
        private const val PROPERTY_SITE_MARKER_SITE_LATITUDE = "site.stream.latitude"
        private const val PROPERTY_SITE_MARKER_SITE_LONGITUDE = "site.stream.longitude"
        private const val PROPERTY_SITE_MARKER_SITE_CREATED_AT = "site.stream.created.at"

        private const val PROPERTY_CLUSTER_TYPE = "cluster.type"

        private const val DEPLOYMENT_CLUSTER = "deployment.cluster"
        private const val POINT_COUNT = "point_count"
        private const val DEPLOYMENT_COUNT = "deployment.count"

        private const val PIN_GREEN = "PIN_GREEN"
        private const val PIN_GREY = "PIN_GREY"
    }

    private lateinit var mapbox: MapboxMap
    private lateinit var style: Style
    private lateinit var symbolManager: SymbolManager

    private var mapSource: GeoJsonSource? = null
    private var lineSource: GeoJsonSource? = null
    private var mapFeatures: FeatureCollection? = null

    private var currentLoc = LatLng()
    private var siteLoc = LatLng()
    private var canMove = false
    private var fromDeploymentList = false
    private lateinit var callback: (LatLng) -> Unit
    private lateinit var mapReadyCallback: (Boolean) -> Unit

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

    fun setParam(canMove: Boolean = true, fromDeploymentList: Boolean = false) {
        this.canMove = canMove
        this.fromDeploymentList = fromDeploymentList
    }

    fun setCameraMoveCallback(callback: (LatLng) -> Unit) {
        this.callback = callback
    }

    fun setMapReadyCallback(callback: (Boolean) -> Unit) {
        this.mapReadyCallback = callback
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
            if (fromDeploymentList) {
                setupSources(it)
                setupImages(it)
                setupMarkerLayers(it)
                setupWindowInfo(it)

                mapboxMap.addOnMapClickListener { latLng ->
                    val screenPoint = mapboxMap.projection.toScreenLocation(latLng)
                    val features = mapboxMap.queryRenderedFeatures(screenPoint, WINDOW_MARKER_ID)
                    val symbolScreenPoint = mapboxMap.projection.toScreenLocation(latLng)
                    if (features.isNotEmpty()) {
                        true
                    } else {
                        true
                    }
                }
            } else {
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

    private fun setupSources(style: Style) {
        mapSource =
            GeoJsonSource(
                SOURCE_DEPLOYMENT,
                FeatureCollection.fromFeatures(listOf()),
                GeoJsonOptions()
                    .withCluster(true)
                    .withClusterMaxZoom(25)
                    .withClusterRadius(30)
                    .withClusterProperty(
                        PROPERTY_CLUSTER_TYPE,
                        Expression.sum(Expression.accumulated(), Expression.get(PROPERTY_CLUSTER_TYPE)),
                        Expression.switchCase(
                            Expression.any(
                                eq(
                                    Expression.get(PROPERTY_DEPLOYMENT_MARKER_IMAGE),
                                    PIN_GREEN
                                )
                            ),
                            Expression.literal(1),
                            Expression.literal(0)
                        )
                    )
            )

        lineSource = GeoJsonSource(SOURCE_LINE)

        style.addSource(mapSource!!)
        style.addSource(lineSource!!)
        mapReadyCallback.invoke(true)
    }

    private fun setupMarkerLayers(style: Style) {

        val line = LineLayer("line-layer", SOURCE_LINE).withProperties(
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
            PropertyFactory.lineWidth(5f),
            PropertyFactory.lineColor(Expression.get("color"))
        )

        style.addLayer(line)

        val unclusteredSiteLayer =
            SymbolLayer(MARKER_SITE_ID, SOURCE_DEPLOYMENT).withProperties(
                PropertyFactory.iconImage("{$PROPERTY_SITE_MARKER_IMAGE}"),
                PropertyFactory.iconSize(0.8f),
                PropertyFactory.iconAllowOverlap(true)
            )

        val unclusteredDeploymentLayer =
            SymbolLayer(MARKER_DEPLOYMENT_ID, SOURCE_DEPLOYMENT).withProperties(
                PropertyFactory.iconImage("{$PROPERTY_DEPLOYMENT_MARKER_IMAGE}"),
                PropertyFactory.iconSize(
                    Expression.match(
                        Expression.toString(
                            Expression.get(
                                PROPERTY_DEPLOYMENT_SELECTED
                            )
                        ),
                        Expression.literal(0.8f), Expression.stop("true", 1.0f)
                    )
                ),
                PropertyFactory.iconAllowOverlap(true)
            )

        style.addLayer(unclusteredSiteLayer)
        style.addLayer(unclusteredDeploymentLayer)

        val layers = arrayOf(
            intArrayOf(0, Color.parseColor("#98A0A9")),
            intArrayOf(1, Color.parseColor("#2AA841"))
        )

        layers.forEachIndexed { i, ly ->
            val deploymentSymbolLayer = CircleLayer("$DEPLOYMENT_CLUSTER-$i", SOURCE_DEPLOYMENT)
            val hasDeploymentAtLeastOne = Expression.toNumber(Expression.get(PROPERTY_CLUSTER_TYPE))
            val pointCount = Expression.toNumber(Expression.get(POINT_COUNT))
            deploymentSymbolLayer.setProperties(PropertyFactory.circleColor(ly[1]), PropertyFactory.circleRadius(16f))
            deploymentSymbolLayer.setFilter(
                if (i == 0) {
                    Expression.all(
                        Expression.gte(hasDeploymentAtLeastOne, Expression.literal(ly[0])),
                        Expression.gte(pointCount, Expression.literal(1))
                    )
                } else {
                    Expression.all(
                        Expression.gte(hasDeploymentAtLeastOne, Expression.literal(ly[0])),
                        Expression.gt(hasDeploymentAtLeastOne, Expression.literal(layers[i - 1][0]))
                    )
                }
            )

            style.addLayer(deploymentSymbolLayer)
        }

        val deploymentCount = SymbolLayer(DEPLOYMENT_COUNT, SOURCE_DEPLOYMENT)
        deploymentCount.setProperties(
            PropertyFactory.textField(
                Expression.format(
                    Expression.formatEntry(
                        Expression.toString(Expression.get(POINT_COUNT)),
                        Expression.FormatOption.formatFontScale(1.5)
                    )
                )
            ),
            PropertyFactory.textSize(12f),
            PropertyFactory.textColor(Color.WHITE),
            PropertyFactory.textIgnorePlacement(true),
            PropertyFactory.textOffset(arrayOf(0f, -0.2f)),
            PropertyFactory.textAllowOverlap(true)
        )

        style.addLayer(deploymentCount)
    }

    private fun setupWindowInfo(it: Style) {
        it.addLayer(
            SymbolLayer(WINDOW_MARKER_ID, SOURCE_DEPLOYMENT).apply {
                withProperties(
                    PropertyFactory.iconImage("{$PROPERTY_WINDOW_INFO_ID}"),
                    PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),
                    PropertyFactory.iconOffset(arrayOf(-2f, -20f)),
                    PropertyFactory.iconAllowOverlap(true)
                )
                withFilter(eq(Expression.get(PROPERTY_DEPLOYMENT_SELECTED), Expression.literal(true)))
            }
        )
    }

    private fun setupImages(style: Style) {
        val drawablePinSite =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_pin_map_grey, null)
        val mBitmapPinSite = BitmapUtils.getBitmapFromDrawable(drawablePinSite)
        if (mBitmapPinSite != null) {
            style.addImage(SITE_MARKER, mBitmapPinSite)
        }

        val drawablePinMapGreen =
            ResourcesCompat.getDrawable(resources, R.drawable.ic_pin_map, null)
        val mBitmapPinMapGreen = BitmapUtils.getBitmapFromDrawable(drawablePinMapGreen)
        if (mBitmapPinMapGreen != null) {
            style.addImage(PIN_GREEN, mBitmapPinMapGreen)
        }
    }

    fun addSiteAndDeploymentToMarker(
        mapMarker: List<MapMarker>
    ) {
        val deploymentFeatures = this.mapFeatures?.features()
        val deploymentSelecting = deploymentFeatures?.firstOrNull { feature ->
            feature.getBooleanProperty(PROPERTY_DEPLOYMENT_SELECTED) ?: false
        }

        // Create point
        val mapMarkerPointFeatures = mapMarker.map {
            // check is this deployment is selecting (to set bigger pin)
            when (it) {
                is MapMarker.DeploymentMarker -> {
                    val deploymentId =
                        deploymentSelecting?.getProperty(PROPERTY_DEPLOYMENT_MARKER_DEPLOYMENT_ID)
                    val isSelecting =
                        if (deploymentSelecting == null || deploymentId == null) {
                            false
                        } else {
                            it.id.toString() == deploymentId.asString
                        }
                    val properties = mapOf(
                        Pair(PROPERTY_DEPLOYMENT_MARKER_LOCATION_ID, "${it.locationName}.${it.id}"),
                        Pair(PROPERTY_WINDOW_INFO_ID, "${it.locationName}.${it.id}"),
                        Pair(PROPERTY_DEPLOYMENT_MARKER_IMAGE, it.pin),
                        Pair(PROPERTY_DEPLOYMENT_MARKER_TITLE, it.locationName),
                        Pair(PROPERTY_DEPLOYMENT_MARKER_DEPLOYMENT_ID, it.id.toString()),
                        Pair(
                            PROPERTY_SITE_MARKER_SITE_CREATED_AT,
                            context?.let { context -> it.deploymentAt.toStringWithTimeZone(context, TimeZone.getDefault()) } ?: ""
                        ),
                        Pair(PROPERTY_DEPLOYMENT_SELECTED, isSelecting.toString())
                    )
                    Feature.fromGeometry(
                        Point.fromLngLat(it.longitude, it.latitude), properties.toJsonObject()
                    )
                }
                is MapMarker.SiteMarker -> {
                    val properties = mapOf(
                        Pair(PROPERTY_SITE_MARKER_IMAGE, it.pin),
                        Pair(PROPERTY_WINDOW_INFO_ID, "${it.name}.${it.id}"),
                        Pair(PROPERTY_SITE_MARKER_SITE_ID, it.id.toString()),
                        Pair(PROPERTY_SITE_MARKER_ID, "${it.name}.${it.id}"),
                        Pair(PROPERTY_SITE_MARKER_SITE_NAME, it.name),
                        Pair(PROPERTY_SITE_MARKER_SITE_LATITUDE, "${it.latitude}"),
                        Pair(PROPERTY_SITE_MARKER_SITE_LONGITUDE, "${it.longitude}"),
                        Pair(
                            PROPERTY_SITE_MARKER_SITE_CREATED_AT,
                            context?.let { context -> it.createdAt.toStringWithTimeZone(context, TimeZone.getDefault()) } ?: ""
                        )
                    )

                    Feature.fromGeometry(
                        Point.fromLngLat(it.longitude, it.latitude), properties.toJsonObject()
                    )
                }
            }
        }
        this.mapFeatures = FeatureCollection.fromFeatures(mapMarkerPointFeatures)
        refreshSource()
    }

    private fun refreshSource() {
        mapSource!!.setGeoJson(mapFeatures)
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
