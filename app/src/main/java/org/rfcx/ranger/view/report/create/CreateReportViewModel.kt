package org.rfcx.ranger.view.report.create

import androidx.lifecycle.ViewModel
import org.rfcx.ranger.entity.response.Response
import org.rfcx.ranger.localdb.ResponseDb

class CreateReportViewModel(private val responseDb: ResponseDb) : ViewModel() {
	fun saveResponseInLocalDb(response: Response) {
		responseDb.save(response)
	}
}
