package org.rfcx.ranger.view.report.create

import android.util.Log
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import okhttp3.ResponseBody
import org.rfcx.ranger.data.remote.response.CreateResponse
import org.rfcx.ranger.entity.response.Response
import org.rfcx.ranger.entity.response.toCreateResponseRequest
import org.rfcx.ranger.localdb.ResponseDb

class CreateReportViewModel(private val createResponse: CreateResponse, private val responseDb: ResponseDb) : ViewModel() {
	fun createResponse(response: Response) {
		createResponse.execute(object : DisposableSingleObserver<ResponseBody>() {
			override fun onSuccess(t: ResponseBody) {
				Log.d("createResponse", "onSuccess $t")
			}
			
			override fun onError(e: Throwable) {
				Log.d("createResponse", "onError $e")
			}
		}, response.toCreateResponseRequest())
	}
	
	fun saveResponseInLocalDb(response: Response) {
		responseDb.save(response)
	}
}
