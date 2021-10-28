package org.rfcx.ranger.view.report.detail

import androidx.lifecycle.ViewModel
import org.rfcx.ranger.entity.response.Response
import org.rfcx.ranger.localdb.ResponseDb

class ResponseDetailViewModel(private val responseDb: ResponseDb) : ViewModel() {
	fun getResponseByCoreId(coreId: String): Response? = responseDb.getResponseByCoreId(coreId)
}
