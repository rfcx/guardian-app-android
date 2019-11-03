package org.rfcx.ranger.view.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.realm.Realm
import io.realm.RealmResults
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.entity.report.ReportImage
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.util.RealmHelper

class ReportDetailViewModel(private val reportDb: ReportDb, private val reportImageDb: ReportImageDb) : ViewModel() {
	
	private var report: Report? = null
	private var reportLive = MutableLiveData<Report>()
	
	private var reportImages: RealmResults<ReportImage>? = null
	private var reportImagesLive = MutableLiveData<List<ReportImage>>()
	
	override fun onCleared() {
		super.onCleared()
		
		report?.removeAllChangeListeners()
	}
	
	fun setReport(id: Int) {
		report?.removeAllChangeListeners()
		report = reportDb.getReportAsync(id)
		report?.addChangeListener<Report> { t ->
			reportLive.value = Report(t.id, t.guid, t.value, t.site, t.reportedAt, t.latitude,
					t.longitude, t.ageEstimateRaw, t.notes, t.audioLocation, t.syncState)
		} ?: run {
			reportLive.value = null
		}
		
		reportImages?.removeAllChangeListeners()
		reportImages = reportImageDb.getByReportIdAsync(id)
		reportImages?.addChangeListener { results ->
			reportImagesLive.value = results.toList()
		}
	}
	
	fun getReport(): LiveData<Report> {
		return reportLive
	}
	
	fun getReportImages(): LiveData<List<ReportImage>> {
		return reportImagesLive
	}
	
	fun addReportImages(imagePaths: List<String>) {
		report?.let {
			val reportImageDb = ReportImageDb(Realm.getInstance(RealmHelper.migrationConfig()))
			reportImageDb.save(it, imagePaths)
		}
	}
	
	fun removeReportImage(imagePath: String) {
		reportImageDb.deleteUnsent(imagePath)
	}
	
}