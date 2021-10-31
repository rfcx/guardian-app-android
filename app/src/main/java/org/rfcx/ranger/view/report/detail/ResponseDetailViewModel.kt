package org.rfcx.ranger.view.report.detail

import androidx.lifecycle.ViewModel
import org.rfcx.ranger.entity.location.TrackingFile
import org.rfcx.ranger.entity.report.ReportImage
import org.rfcx.ranger.entity.response.Response
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.localdb.ResponseDb
import org.rfcx.ranger.localdb.TrackingFileDb

class ResponseDetailViewModel(private val responseDb: ResponseDb, private val reportImageDb: ReportImageDb, private val trackingFileDb: TrackingFileDb) : ViewModel() {
	fun getResponseByCoreId(coreId: String): Response? = responseDb.getResponseByCoreId(coreId)
	
	fun getImagesByCoreId(coreId: String): List<ReportImage> = reportImageDb.getByCoreId(coreId)
	
	fun getTrackingByCoreId(coreId: String): TrackingFile? = trackingFileDb.getByCoreId(coreId)
}
