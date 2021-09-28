package org.rfcx.ranger.view.report.create

import android.util.Log
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import okhttp3.ResponseBody
import org.rfcx.ranger.data.remote.response.CreateResponse
import org.rfcx.ranger.data.remote.response.CreateResponseRequest
import org.rfcx.ranger.util.toIsoString
import java.util.*

class CreateReportViewModel(private val createResponse: CreateResponse) : ViewModel() {
	fun createResponse() {
		createResponse.execute(object : DisposableSingleObserver<ResponseBody>() {
			override fun onSuccess(t: ResponseBody) {
				Log.d("createResponse", "onSuccess $t")
			}
			
			override fun onError(e: Throwable) {
				Log.d("createResponse", "onError $e")
			}
		}, CreateResponseRequest(Date().toIsoString(), Date().toIsoString(), Date().toIsoString(), listOf(101, 103), 1, 1, listOf(201, 203), "I found a machete", "skhedl36rxb2"))
	}
}
