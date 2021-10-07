package org.rfcx.ranger.view.report.create

import androidx.lifecycle.ViewModel
import org.rfcx.ranger.entity.response.Response
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.localdb.ResponseDb

class CreateReportViewModel(private val responseDb: ResponseDb, private val reportImageDb: ReportImageDb) : ViewModel() {
	fun saveResponseInLocalDb(response: Response) {
		responseDb.save(response)
	}
	
	fun getResponseById(id: Int): Response? = responseDb.getResponseById(id)
	
	fun saveImages(response: Response, images: List<String>) {
		reportImageDb.deleteImages(response.id)
		reportImageDb.save(response, images)
	}
}
