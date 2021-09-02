package org.rfcx.ranger.view.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.rfcx.ranger.R
import org.rfcx.ranger.view.base.BaseFragment


class MapFragment : BaseFragment() {
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_map, container, false)
	}
	
	companion object {
		const val tag = "MapFragment"
		
		fun newInstance(): MapFragment = MapFragment()
	}
}
