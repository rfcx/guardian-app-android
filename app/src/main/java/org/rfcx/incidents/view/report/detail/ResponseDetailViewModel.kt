package org.rfcx.incidents.view.report.detail

import androidx.lifecycle.ViewModel
import org.rfcx.incidents.data.local.ReportImageDb
import org.rfcx.incidents.data.local.ResponseDb
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.local.TrackingFileDb
import org.rfcx.incidents.entity.location.TrackingFile
import org.rfcx.incidents.entity.response.ImageAsset
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.stream.Stream

class ResponseDetailViewModel(
    private val responseDb: ResponseDb,
    private val reportImageDb: ReportImageDb,
    private val streamDb: StreamDb,
    private val trackingFileDb: TrackingFileDb
) : ViewModel() {
    fun getResponseByCoreId(coreId: String): Response? = responseDb.getResponseByCoreId(coreId)

    fun getImagesByCoreId(coreId: String): List<ImageAsset> = reportImageDb.getByCoreId(coreId)

    fun getTrackingByCoreId(coreId: String): TrackingFile? = trackingFileDb.getByCoreId(coreId)

    fun getStream(serverId: String): Stream? = streamDb.get(serverId)
}
