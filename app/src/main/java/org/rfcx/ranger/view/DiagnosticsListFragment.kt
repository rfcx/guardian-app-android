package org.rfcx.ranger.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_diagnastic_list.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.location.LocationAdapter

class DiagnosticsListFragment : Fragment() {
	
	companion object {
		fun newInstance(): DiagnosticsListFragment = DiagnosticsListFragment()
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_diagnastic_list, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setupAdapter()
	}
	
	private fun setupAdapter() {
		locationRecycler.apply {
			layoutManager = LinearLayoutManager(context)
			setHasFixedSize(true)
			adapter = LocationAdapter()
		}
	}
	
}