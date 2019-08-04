package org.rfcx.ranger.view.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import org.rfcx.ranger.R
import org.rfcx.ranger.view.base.BaseFragment

class MapFragment : BaseFragment(), OnMapReadyCallback {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}
	
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
	
	override fun onMapReady(p0: GoogleMap?) {
		p0?.let {
//			drawPolyLineToMap(it) // create line check-in
//			createMarkReports(it) // create mark of Reports
		}
	}
	
	
	companion object {
		fun newInstance(): MapFragment {
			return MapFragment()
		}
		const val tag = "MapFragment"
	}
}