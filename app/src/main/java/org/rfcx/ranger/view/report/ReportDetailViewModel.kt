package org.rfcx.ranger.view.report

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import io.realm.Realm
import io.realm.RealmResults
import okhttp3.ResponseBody
import org.rfcx.ranger.data.remote.shortlink.ShortLinkUseCase
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.entity.report.ReportImage
import org.rfcx.ranger.entity.shortlink.ShortLinkRequest
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.util.RealmHelper

class ReportDetailViewModel(private val reportDb: ReportDb, private val reportImageDb: ReportImageDb, private val shortLinkUseCase: ShortLinkUseCase) : ViewModel() {
	
	private var report: Report? = null
	private var reportLive = MutableLiveData<Report>()
	
	private var reportImages: RealmResults<ReportImage>? = null
	private var reportImagesLive = MutableLiveData<List<ReportImage>>()
	
	private var _shortLink: MutableLiveData<String> = MutableLiveData()
	val shortLink: LiveData<String>
		get() = _shortLink
	
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
	
	fun getShortLink(url: String) {
		
		shortLinkUseCase.execute(object : DisposableSingleObserver<ResponseBody>() {
			override fun onError(e: Throwable) {
				Log.d("shortLinkUseCase", "onError ${e.message}")
			}
			
			override fun onSuccess(t: ResponseBody) {
				Log.d("shortLinkUseCase", t.string())
			}
			
		}, ShortLinkRequest(url, "temp", "86400000"))
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
	
	fun saveEditedNoteIfChanged(newNote: String?) {
		if (newNote != reportLive.value?.notes) {
			val editedReport = reportLive.value?.let { t ->
				Report(t.id, t.guid, t.value, t.site, t.reportedAt, t.latitude,
						t.longitude, t.ageEstimateRaw, newNote, t.audioLocation, t.syncState)
			}
			editedReport?.let {
				editedReport.syncState = ReportDb.UNSENT
				reportDb.update(editedReport)
			}
		}
	}
}