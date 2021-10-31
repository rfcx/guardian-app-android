package org.rfcx.ranger.view.report.detail

import androidx.lifecycle.ViewModel
import org.rfcx.ranger.entity.report.ReportImage
import org.rfcx.ranger.entity.response.Response
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.localdb.ResponseDb

class ResponseDetailViewModel(private val responseDb: ResponseDb, private val reportImageDb: ReportImageDb) : ViewModel() {
	fun getResponseByCoreId(coreId: String): Response? = responseDb.getResponseByCoreId(coreId)
	
	fun getImagesByCoreId(coreId: String): List<ReportImage> = reportImageDb.getByCoreId(coreId)
}
