package org.rfcx.ranger.view.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_report_list.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.entity.report.ReportImage
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.localdb.ReportImageDb

class ReportListFragment : Fragment() {
	
	companion object {
		const val tag = "ReportListFragment"
		fun newInstance(): ReportListFragment = ReportListFragment()
	}
	
	private val reportAdapter = ReportListAdapter()
	private lateinit var reportDB: ReportDb
	private lateinit var reportImageDb: ReportImageDb
	private lateinit var allReportResults: RealmResults<Report>
	private lateinit var allReportImageResults: RealmResults<ReportImage>
	private var isAllReportResultsReady = false
	private var isAllReportImageResultsReady = false
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater.inflate(R.layout.fragment_report_list, container, false)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		setUpAdapter()
	}
	
	override fun onStart() {
		super.onStart()
		initDatabase()
		observeRealmChange()
	}
	
	override fun onStop() {
		super.onStop()
		allReportResults.removeAllChangeListeners()
		allReportImageResults.removeAllChangeListeners()
	}
	
	private fun setUpAdapter() {
		reportAdapter.apply {
			setEmptyView(R.string.report_list_empty_message, null)
			onItemClick = { position ->
				val reportId = reportAdapter.getItemAt(position)?.getId()
				if (reportId != null) {
					context?.let { ReportActivity.startIntent(it, reportId) }
				}
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
		reportSwipeRefresh.isRefreshing = true
		val reportItems = arrayListOf<ReportItem>()
		
		val reports = reportDB.getAllAsync()
		reports.forEach {
			val images = reportImageDb.getSync(it.id)
			val unsentCount = images.filter { reportImage ->
				reportImage.syncState != ReportImageDb.SENT
			}.count()
			reportItems.add(ReportItem(it, images.count(), unsentCount))
		}
		reportAdapter.setReports(reportItems)
		reportSwipeRefresh.isRefreshing = false
		reportSwipeRefresh.isEnabled = false
	}
	
	private fun observeRealmChange() {
		allReportResults = reportDB.getAllResultsAsync()
		allReportImageResults = reportImageDb.getAllResultsAsync()
		
		allReportResults.addChangeListener { _, _ ->
			isAllReportResultsReady = true
			if (isAllReportImageResultsReady) {
				loadReport()
			}
		}
		
		allReportImageResults.addChangeListener { _, _ ->
			isAllReportImageResultsReady = true
			if (isAllReportResultsReady) {
				loadReport()
			}
		}
	}
	
	private fun initDatabase() {
		reportDB = ReportDb()
		reportImageDb = ReportImageDb()
	}
}