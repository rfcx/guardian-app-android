package org.rfcx.ranger.view.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.localdb.ReportImageDb

class ReportDetailViewModel(private val reportDb: ReportDb, private val reportImageDb: ReportImageDb) : ViewModel() {
	
	private var report: Report? = null
	private var reportLive = MutableLiveData<Report>()
	
	override fun onCleared() {
		super.onCleared()
		
		report?.removeAllChangeListeners()
	}
	
	fun setReport(id: Int) {
		report?.removeAllChangeListeners()
		report = reportDb.getReportAsync(id)
		report?.addChangeListener<Report> { t ->
			reportLive.value = Report(t.id, t.guid, t.value, t.site, t.reportedAt, t.latitude, t.longitude,
					t.ageEstimateRaw, t.audioLocation, t.syncState)
		} ?: run {
			reportLive.value = null
		}
	}
	
	fun getReport(): LiveData<Report?> {
		return reportLive
	}
	
	
}