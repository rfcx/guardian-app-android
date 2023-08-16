package org.rfcx.incidents.view.base

import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import org.rfcx.incidents.entity.stream.MarkerItem

abstract class BaseMapFragment : BaseFragment(),
    OnMapReadyCallback,
    ClusterManager.OnClusterClickListener<MarkerItem>,
    ClusterManager.OnClusterItemClickListener<MarkerItem> {
    var map: GoogleMap? = null
    var mapView: SupportMapFragment? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mClusterManager: ClusterManager<MarkerItem>

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        Log.d("mapView", "onMapReady")
    }

    override fun onClusterClick(cluster: Cluster<MarkerItem>?): Boolean {
        Log.d("mapView", "onClusterClick")
        return true
    }

    override fun onClusterItemClick(item: MarkerItem?): Boolean {
        Log.d("mapView", "onClusterItemClick")
        return false
    }
}
