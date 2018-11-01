package org.rfcx.ranger.repo.api

import android.content.Context
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.entity.report.SendReportResponse
import org.rfcx.ranger.util.getEmail
import org.rfcx.ranger.util.getTokenID
import org.rfcx.ranger.util.getUserGuId
import android.util.Log
import com.crashlytics.android.Crashlytics
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.repo.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SendReportApi {

	val tag = "SendReportApi"
	
	fun sendReport(context: Context, report: Report, sendReportCallback: SendReportCallback) {
		
		val guid = context.getUserGuId()
		val token = context.getTokenID()
		val email = context.getEmail()
		if (guid == null || token == null || email == null) {
			sendReportCallback.onFailed(TokenExpireException(context), null)
			return
		}

		val authUser = "Bearer $token"
		
		Log.d(tag, report.toString())
		ApiManager.getInstance().apiRest.sendReport(authUser, report)
				.enqueue(object : Callback<SendReportResponse> {
					override fun onFailure(call: Call<SendReportResponse>?, t: Throwable?) {
						Crashlytics.logException(t)
						sendReportCallback.onFailed(t, t?.message)
					}
					
					override fun onResponse(call: Call<SendReportResponse>?, response: Response<SendReportResponse>?) {
						val result = responseParser(response)
						when (result) {
							is Ok -> {
								sendReportCallback.onSuccess()
							}
							is Err -> {
								responseErrorHandler(result.error, sendReportCallback, context, tag)
							}
						}
					}
					
				})
		
	}
	
	
	interface SendReportCallback: ApiCallback {
		fun onSuccess()
	}
}