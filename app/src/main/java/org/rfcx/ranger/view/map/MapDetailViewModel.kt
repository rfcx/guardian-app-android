package org.rfcx.ranger.view.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import io.realm.Realm
import io.realm.RealmResults
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.entity.report.ReportImage
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.localdb.ReportImageDb.Companion.SENT
import org.rfcx.ranger.util.asLiveData

class MapDetailViewModel(private val reportDb: ReportDb, private val reportImageDb: ReportImageDb) : ViewModel() {
	
	private var _report: Report? = null
	
	private var _reportLive = MutableLiveData<Report>()
	
	private val report: LiveData<Report?>
		get() = _reportLive
	
	fun getReportDetail(reportId: Int): LiveData<Report?> {
		_report = reportDb.getReportAsync(reportId)
		_report?.addChangeListener<Report> { t ->
			if (t.isValid)
				_reportLive.value = Realm.getDefaultInstance().copyFromRealm(t)
			else _reportLive.value = null
		} ?: run {
			_reportLive.value = null
		}
		
		return report
	}
	
	fun getReportImages(reportId: Int): LiveData<ImageState> {
		return Transformations.map<RealmResults<ReportImage>, ImageState>(
				reportImageDb.getByReportIdAsync(reportId).asLiveData()
		) { it ->
			ImageState(it.count(), it.filter { it.syncState != SENT }.count())
		}
	}
	
	override fun onCleared() {
		super.onCleared()
		_report?.removeAllChangeListeners()
	}
}

data class ImageState(val count: Int, val unsentCount: Int)