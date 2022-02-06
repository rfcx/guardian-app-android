package org.rfcx.incidents.view.report.detail

import androidx.lifecycle.ViewModel
import org.rfcx.incidents.data.local.ReportImageDb
import org.rfcx.incidents.data.local.ResponseDb
import org.rfcx.incidents.data.local.TrackingFileDb
import org.rfcx.incidents.entity.location.TrackingFile
import org.rfcx.incidents.entity.response.ImageAsset
import org.rfcx.incidents.entity.response.Response

class ResponseDetailViewModel(
    private val responseDb: ResponseDb,
    private val reportImageDb: ReportImageDb,
    private val trackingFileDb: TrackingFileDb
) : ViewModel() {
    fun getResponseByCoreId(coreId: String): Response? = responseDb.getResponseByCoreId(coreId)

    fun getImagesByCoreId(coreId: String): List<ImageAsset> = reportImageDb.getByCoreId(coreId)

    fun getTrackingByCoreId(coreId: String): TrackingFile? = trackingFileDb.getByCoreId(coreId)
}
