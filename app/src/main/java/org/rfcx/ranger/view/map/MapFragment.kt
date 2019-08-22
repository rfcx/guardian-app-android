package org.rfcx.ranger.view.map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.util.DateHelper
import org.rfcx.ranger.view.MainActivityEventListener
import org.rfcx.ranger.view.base.BaseFragment

class MapFragment : BaseFragment(), OnMapReadyCallback {
	
	private val mapViewModel: MapViewModel by viewModel()
	private var checkInPolyline: Polyline? = null
	private var checkInMarkers = arrayListOf<Marker>()
	private var retortMarkers = arrayListOf<Marker>()
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_map, container, false)
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
		map?.getMapAsync(this)
	}
	
	override fun onMapReady(map: GoogleMap?) {
		map?.let {
			it.mapType = GoogleMap.MAP_TYPE_SATELLITE
			displayReport(it)
			displayCheckIn(it)
			map.setOnMapClickListener {
				(activity as MainActivityEventListener).hideBottomSheet()
			}
		}
	}
	
	private fun displayReport(map: GoogleMap) {
		
		map.setOnMarkerClickListener { marker ->
			
			Log.d(tag, "Map click $marker")
			if (marker.tag is Report) {
				val report = marker.tag as Report
				(activity as MainActivityEventListener)
						.showBottomSheet(MapDetailBottomSheetFragment.newInstance(report.id))
			} else {
				(activity as MainActivityEventListener).hideBottomSheet()
			}
			false
		}
		
		mapViewModel.getReports().observe(this, Observer { reports ->
			
			if (!isAdded || isDetached) return@Observer
			
			Log.d(tag, "${reports.count()}")
			
			retortMarkers.forEach {
				it.remove()
			}
			retortMarkers.clear()
			
			for (report in reports) {
				val latLng = LatLng(report.latitude, report.longitude)
				val marker = map.addMarker(MarkerOptions()
						.position(latLng)
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_report_pin_on_map)))
				marker.tag = report
				marker.zIndex = 1f
				retortMarkers.add(marker)
			}
		})
	}
	
	private fun displayCheckIn(map: GoogleMap) {
		// clear old markers
		checkInMarkers.forEach {
			it.remove()
		}
		checkInPolyline?.remove()
		checkInMarkers.clear()
		
		mapViewModel.getCheckIns().observe(this, Observer { checkIns ->
			
			if (!isAdded || isDetached) return@Observer
			
			Log.d(tag, "${checkIns.count()}")
			
			val polylineOptions = PolylineOptions()
			context?.let {
				val color = ContextCompat.getColor(it, R.color.check_in_polyline)
				polylineOptions.color(color)
			}
			
			for (checkIn in checkIns) {
				val latLng = LatLng(checkIn.latitude, checkIn.longitude)
				polylineOptions.add(latLng)
				checkInMarkers.add(map.addMarker(MarkerOptions()
						.position(latLng)
						.anchor(0.5f, 0.5f)
						.title(DateHelper.parse(checkIn.time))
						.snippet("${checkIn.latitude},${checkIn.latitude}")
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_chek_in_pin_on_map))))
				checkInPolyline = map.addPolyline(polylineOptions)
			}
			
			if (checkIns.isNotEmpty()) {
				val lastCheckIn = checkIns.last()
				moveMapTo(map, LatLng(lastCheckIn.latitude, lastCheckIn.longitude))
			}
		})
	}
	
	private fun moveMapTo(googleMap: GoogleMap, latLng: LatLng) {
		Log.d(tag, "moveMapTo $latLng")
		if (!isAdded || isDetached) return
		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
				LatLng(latLng.latitude, latLng.longitude), 18f))
	}
	
	companion object {
		fun newInstance(): MapFragment {
			return MapFragment()
		}
		
		const val tag = "MapFragment"
	}
}