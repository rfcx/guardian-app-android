package org.rfcx.ranger.view.report.create

import androidx.lifecycle.ViewModel
import org.rfcx.ranger.entity.report.ReportImage
import org.rfcx.ranger.entity.response.Response
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.localdb.ResponseDb

class CreateReportViewModel(private val responseDb: ResponseDb, private val reportImageDb: ReportImageDb) : ViewModel() {
	
	fun getImagesFromLocal(id: Int): List<ReportImage> = reportImageDb.getByReportId(id)
	
	fun saveResponseInLocalDb(response: Response, images: List<String>?) {
		val res = responseDb.save(response)
		if (!images.isNullOrEmpty()) {
			reportImageDb.deleteImages(res.id)
			reportImageDb.save(res, images)
		}
	}
	
	fun getResponseById(id: Int): Response? = responseDb.getResponseById(id)
}
