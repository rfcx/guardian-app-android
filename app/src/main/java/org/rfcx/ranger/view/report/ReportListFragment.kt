package org.rfcx.ranger.view.report

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_report_list.*
import org.rfcx.ranger.R
import org.rfcx.ranger.localdb.ReportDb

class ReportListFragment : Fragment() {
	
	companion object {
		const val tag = "ReportListFragment"
		fun newInstance(): ReportListFragment = ReportListFragment()
	}
	
	private val reportAdapter = ReportListAdapter()
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_report_list, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setUpAdapter()
		loadReport()
	}
	
	private fun setUpAdapter() {
		reportAdapter.apply {
			setEmptyView(R.string.report_list_empty_message, null)
			onItemClick = {
				// TODO get report item n Open report detail
			}
		}
		context?.let {
			reportRecycler.apply {
				layoutManager = LinearLayoutManager(it)
				setHasFixedSize(true)
				adapter = reportAdapter
			}
		}
	}
	
	private fun loadReport() {
		Log.d(tag, "loadReport")
		reportSwipeRefresh.isRefreshing = true
		val reportDB = ReportDb()
		reportAdapter.setReports(reportDB.getAllAsync())
		reportSwipeRefresh.isRefreshing = false
		reportSwipeRefresh.isEnabled = false
	}
}