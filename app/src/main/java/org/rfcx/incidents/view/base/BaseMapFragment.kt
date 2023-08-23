package org.rfcx.incidents.view.base

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.stream.MarkerDetail
import org.rfcx.incidents.entity.stream.MarkerItem
import org.rfcx.incidents.util.LocationPermissions
import org.rfcx.incidents.view.events.InfoWindowAdapter
import org.rfcx.incidents.view.events.MarkerRenderer

abstract class BaseMapFragment : BaseFragment(),
    OnMapReadyCallback,
    ClusterManager.OnClusterClickListener<MarkerItem>,
    ClusterManager.OnClusterItemClickListener<MarkerItem>,
    ClusterManager.OnClusterItemInfoWindowClickListener<MarkerItem> {

    var map: GoogleMap? = null
    var mapView: SupportMapFragment? = null

    lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mClusterManager: ClusterManager<MarkerItem>
    private lateinit var myClusterRenderer: MarkerRenderer

    private val locationPermissions by lazy { LocationPermissions(requireActivity()) }
    private var lastLocation: Location? = null
    private var siteLoc = LatLng(0.0, 0.0)
    private var currentLoc = LatLng(0.0, 0.0)

    private lateinit var callback: (LatLng) -> Unit
    private lateinit var seeDetailCallback: (Int) -> Unit
    private lateinit var openStreamDetailCallback: (String, String, Double) -> Unit

    fun setGoogleMap(mMap: GoogleMap, canMove: Boolean) {
        map = mMap
        mMap.uiSettings.setAllGesturesEnabled(canMove)
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.isZoomControlsEnabled = false
        addClusteredMarkers(mMap)

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.style_json
                )
            )
        } catch (_: Resources.NotFoundException) {
        }
    }

    private fun addClusteredMarkers(googleMap: GoogleMap) {
        // Create the ClusterManager class and set the custom renderer.
        mClusterManager = ClusterManager<MarkerItem>(requireContext(), googleMap)
        myClusterRenderer = MarkerRenderer(
            requireContext(),
            googleMap,
            mClusterManager
        )
        mClusterManager.renderer = myClusterRenderer

        // Set custom info window adapter
        mClusterManager.markerCollection.setInfoWindowAdapter(InfoWindowAdapter(requireContext()))

        // Set ClusterManager as the OnCameraIdleListener so that it
        // can re-cluster when zooming in and out.
        googleMap.setOnCameraIdleListener {
            mClusterManager.onCameraIdle()
        }

        googleMap.setOnMarkerClickListener(mClusterManager)
        googleMap.setInfoWindowAdapter(mClusterManager.markerManager)
        googleMap.setOnInfoWindowClickListener(mClusterManager)
        mClusterManager.setOnClusterClickListener(this)
        mClusterManager.setOnClusterItemClickListener(this)
        mClusterManager.setOnClusterItemInfoWindowClickListener(this)
    }

    fun setMarker(mapMarker: MutableList<MarkerItem>) {
        mClusterManager.clearItems()
        mClusterManager.cluster()

        mClusterManager.addItems(mapMarker)
        mClusterManager.cluster()

        if (mapMarker.isEmpty()) return
        moveCamera(mapMarker.last().position)
    }

    fun setCallback(mMap: GoogleMap) {
        mMap.setOnCameraMoveListener {
            val currentCameraPosition = mMap.cameraPosition.target
            if (::callback.isInitialized) {
                this.callback.invoke(LatLng(currentCameraPosition.latitude, currentCameraPosition.longitude))
            }
        }
    }

    override fun onClusterClick(cluster: Cluster<MarkerItem>?): Boolean {
        val builder = LatLngBounds.builder()
        val markers: Collection<MarkerItem> = cluster!!.items

        for (item in markers) {
            val position = item.position
            builder.include(position)
        }

        val bounds = builder.build()

        try {
            map?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        } catch (error: Exception) {
            return true
        }
        return true
    }

    override fun onClusterItemClick(item: MarkerItem?): Boolean {
        if (item?.snippet?.isNotBlank() == true) {
            val data = Gson().fromJson(item.snippet, MarkerDetail::class.java)
            if (!data.fromDeployment) {
                myClusterRenderer.getMarker(item).hideInfoWindow()
                openStreamDetailCallback.invoke(data.name, data.serverId, data.distance)
            }
        }
        return false
    }

    override fun onClusterItemInfoWindowClick(item: MarkerItem?) {
        if (item?.snippet?.isNotBlank() == true) {
            val data = Gson().fromJson(item.snippet, MarkerDetail::class.java)
            if (data.fromDeployment) {
                if (data.infoWindowMarker?.isDeployment == true) {
                    seeDetailCallback.invoke(data.id)
                }
            }
        }
    }

    fun fusedLocationClient() {
        if (map == null) return
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissions.check { }
            return
        }

        map?.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            map?.uiSettings?.isZoomControlsEnabled = false
            map?.uiSettings?.isMyLocationButtonEnabled = false
            lastLocation = location
        }
    }

    fun setSiteLocation(siteLoc: LatLng) {
        this.siteLoc = siteLoc
        if (map != null) {
            moveCamera(siteLoc)
        }
    }

    fun addMarker(latLng: LatLng) {
        map?.clear()
        map?.addMarker(
            MarkerOptions().position(latLng).icon(bitmapFromVector(requireContext(), R.drawable.ic_pin_map))
        )
    }

    fun moveCamera(latLng: LatLng) {
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
    }

    fun setSeeDetailCallback(callback: (Int) -> Unit) {
        this.seeDetailCallback = callback
    }

    fun setOpenStreamDetailCallback(callback: (String, String, Double) -> Unit) {
        this.openStreamDetailCallback = callback
    }

    fun moveCamera(location: Location?) {
        if (location == null) return
        val latLng = LatLng(location.latitude, location.longitude)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
    }

    fun setCameraMoveCallback(callback: (LatLng) -> Unit) {
        this.callback = callback
    }

    fun setCurrentLocation(currentLoc: LatLng) {
        this.currentLoc = currentLoc
    }

    fun getLastLocation(): Location? {
        return this.lastLocation
    }

    fun getCurrentPosition(): LatLng? {
        return map?.cameraPosition?.target
    }

    private fun bitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable: Drawable = ContextCompat.getDrawable(context, vectorResId)!!
        vectorDrawable.setBounds(
            0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight
        )
        val bitmap: Bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )

        val canvas: Canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    companion object {
        private const val DEFAULT_ZOOM = 15.0f
    }
}
