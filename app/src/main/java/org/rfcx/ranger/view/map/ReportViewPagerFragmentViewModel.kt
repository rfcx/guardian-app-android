package org.rfcx.ranger.view.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.util.asLiveData

class ReportViewPagerFragmentViewModel(private val reportDb: ReportDb) : ViewModel() {
	
	fun getReports(firstReportId: Int?): LiveData<List<Report>> {
		return Transformations.map(
				reportDb.getAllResultsAsync().asLiveData()
		) { it ->
			it.sortedBy {
				if (it.id == firstReportId) 0
				else 1
			}
		}
	}
}