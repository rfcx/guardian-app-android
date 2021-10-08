package org.rfcx.ranger.view.report.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.entity.report.ReportImage
import org.rfcx.ranger.entity.response.Response
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.localdb.ResponseDb
import org.rfcx.ranger.util.asLiveData

class CreateReportViewModel(private val responseDb: ResponseDb, private val reportImageDb: ReportImageDb) : ViewModel() {
	
	fun getImagesFromLocal(id: Int): LiveData<List<ReportImage>> {
		return Transformations.map(reportImageDb.getByReportIdAsync(id).asLiveData()) { it }
	}
	
	fun saveResponseInLocalDb(response: Response) {
		responseDb.save(response)
	}
	
	fun getResponseById(id: Int): Response? = responseDb.getResponseById(id)
	
	fun saveImages(response: Response, images: List<String>) {
		reportImageDb.deleteImages(response.id)
		reportImageDb.save(response, images)
	}
}
