package org.rfcx.ranger.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import kotlinx.android.synthetic.main.fragment_diagnastic_list.*
import org.rfcx.ranger.R
import org.rfcx.ranger.adapter.SyncInfo
import org.rfcx.ranger.adapter.location.LocationAdapter
import org.rfcx.ranger.service.LocationSyncWorker
import org.rfcx.ranger.service.ReportSyncWorker

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

		LocationSyncWorker.workInfos().observe(this,
				Observer<List<WorkInfo>> { workStatusList ->
					val currentWorkStatus = workStatusList?.getOrNull(0)
					if (currentWorkStatus != null &&
							(currentWorkStatus.state == WorkInfo.State.SUCCEEDED || currentWorkStatus.state == WorkInfo.State.FAILED)) {
						locationRecycler.adapter?.notifyDataSetChanged()
					}
				})
	}

	private fun setupAdapter() {
		locationRecycler.apply {
			layoutManager = LinearLayoutManager(context)
			setHasFixedSize(true)
			adapter = LocationAdapter()
		}
	}
	
}