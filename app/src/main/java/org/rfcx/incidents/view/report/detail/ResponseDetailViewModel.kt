package org.rfcx.incidents.view.report.detail

import androidx.lifecycle.ViewModel
import org.rfcx.incidents.entity.location.TrackingFile
import org.rfcx.incidents.entity.report.ReportImage
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.localdb.ReportImageDb
import org.rfcx.incidents.localdb.ResponseDb
import org.rfcx.incidents.localdb.TrackingFileDb

class ResponseDetailViewModel(
    private val responseDb: ResponseDb,
    private val reportImageDb: ReportImageDb,
    private val trackingFileDb: TrackingFileDb
) : ViewModel() {
    fun getResponseByCoreId(coreId: String): Response? = responseDb.getResponseByCoreId(coreId)
    
    fun getImagesByCoreId(coreId: String): List<ReportImage> = reportImageDb.getByCoreId(coreId)
    
    fun getTrackingByCoreId(coreId: String): TrackingFile? = trackingFileDb.getByCoreId(coreId)
}
