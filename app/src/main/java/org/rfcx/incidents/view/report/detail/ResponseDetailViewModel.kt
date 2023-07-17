package org.rfcx.incidents.view.report.detail

import androidx.lifecycle.ViewModel
import org.rfcx.incidents.data.local.ResponseDb
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.stream.Stream

class ResponseDetailViewModel(
    private val responseDb: ResponseDb,
    private val streamDb: StreamDb
) : ViewModel() {
    fun getResponseByCoreId(coreId: String): Response? = responseDb.getResponseByCoreId(coreId)

    fun getStream(serverId: String): Stream? = streamDb.get(serverId, false)
}
