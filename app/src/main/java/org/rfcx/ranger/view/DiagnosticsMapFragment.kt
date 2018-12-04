package org.rfcx.ranger.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import org.rfcx.ranger.R
import org.rfcx.ranger.util.DateHelper
import org.rfcx.ranger.util.RealmHelper

class DiagnosticsMapFragment : Fragment(), OnMapReadyCallback {
	
	companion object {
		fun newInstance(): DiagnosticsMapFragment = DiagnosticsMapFragment()
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_diagnostics_map, container, false)
		
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupMap()
	}
	
	override fun onDestroyView() {
		val map = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
		map?.let {
			childFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
		}
		super.onDestroyView()
	}
	
	private fun setupMap() {
		val map = childFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment?
		map?.getMapAsync(this@DiagnosticsMapFragment)
	}
	
	override fun onMapReady(p0: GoogleMap?) {
		p0?.let {
			drawPolyLineToMap(it)
		}
	}
	
	private fun drawPolyLineToMap(googleMap: GoogleMap) {
		if (context == null || !isAdded) return
		val polylineOptions = PolylineOptions()
		context?.let {
			val color = ContextCompat.getColor(it, R.color.colorPrimary)
			polylineOptions.color(color)
		}
		
		// TODO filter lasted 6 hr location
		val rangerLocations = RealmHelper.getInstance().getLocations()
		if(rangerLocations.isEmpty()) return
		for (location in rangerLocations) {
			polylineOptions.add(LatLng(location.latitude, location.longitude))
			
			val latLng = LatLng(location.latitude, location.longitude)
			
			googleMap.addMarker(MarkerOptions()
					.position(latLng)
					.title(DateHelper.parse(location.time))
					.snippet("${location.latitude},${location.latitude}")
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin)))
		}
		googleMap.addPolyline(polylineOptions)
		
		val lastLocation = rangerLocations.last()
		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
				LatLng(lastLocation.latitude, lastLocation.longitude), 18f))
	}
}