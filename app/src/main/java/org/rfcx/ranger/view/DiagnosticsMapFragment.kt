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
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.localdb.LocationDb
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.util.DateHelper

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
			drawPolyLineToMap(it) // create line check-in
			createMarkReports(it) // create mark of Reports
		}
	}

	private fun createMarkReports(googleMap: GoogleMap) {
		if (context == null || !isAdded) return
		val reports = ReportDb().getAllAsync()
		if (reports.isEmpty()) return

		for (report in reports) {
			val latLng = LatLng(report.latitude, report.longitude)
			val marker = googleMap.addMarker(MarkerOptions()
					.position(latLng)
					.title(report.site)
					.snippet(DateHelper.parse(report.reportedAt)))
			marker.tag = report
		}

		googleMap.setOnInfoWindowClickListener { marker ->
			if (marker.tag != null && marker.tag is Report) {
				val report = marker.tag as Report
				context?.let { ReportActivity.startIntent(it, report.id) }
			}
		}
	}

	private fun drawPolyLineToMap(googleMap: GoogleMap) {
		if (context == null || !isAdded) return
		val polylineOptions = PolylineOptions()
		context?.let {
			val color = ContextCompat.getColor(it, R.color.colorPrimary)
			polylineOptions.color(color)
		}

		val checkins = LocationDb().allForDisplay()
		if(checkins.isEmpty()) return
		for (checkin in checkins) {
			val latLng = LatLng(checkin.latitude, checkin.longitude)
			polylineOptions.add(latLng)
			googleMap.addMarker(MarkerOptions()
					.position(latLng)
					.title(DateHelper.parse(checkin.time))
					.snippet("${checkin.latitude},${checkin.latitude}")
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin)))
		}
		googleMap.addPolyline(polylineOptions)
		
		checkins.last()?.let {
			googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
					LatLng(it.latitude, it.longitude), 18f))
		}

	}
}