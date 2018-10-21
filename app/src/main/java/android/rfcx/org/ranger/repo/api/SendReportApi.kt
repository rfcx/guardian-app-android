package android.rfcx.org.ranger.repo.api

import android.content.Context
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.entity.ErrorResponse
import android.rfcx.org.ranger.entity.report.Report
import android.rfcx.org.ranger.entity.report.SendReportResponse
import android.rfcx.org.ranger.repo.ApiManager
import android.rfcx.org.ranger.repo.TokenExpireException
import android.rfcx.org.ranger.util.GsonProvider
import android.rfcx.org.ranger.util.getEmail
import android.rfcx.org.ranger.util.getTokenID
import android.rfcx.org.ranger.util.getUserGuId
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Jingjoeh on 10/22/2017 AD.
 */

class SendReportApi {
	
	fun sendReport(context: Context, report: Report, sendReportCallback: SendReportCallback) {
		
		val guid = context.getUserGuId()
		val token = context.getTokenID()
		val email = context.getEmail()
		if (guid == null || token == null || email == null) {
			sendReportCallback.onFailed(TokenExpireException(context), null)
			return
		}
		
		//val authUser = "user/$guid"
		val authUser = "Bearer $token"
		
		Log.d("SendReportApi", report.toString())
		ApiManager.getInstance().apiRest.sendReport(authUser, report)
				.enqueue(object : Callback<SendReportResponse> {
					override fun onFailure(call: Call<SendReportResponse>?, t: Throwable?) {
						sendReportCallback.onFailed(t, t?.message)
					}
					
					override fun onResponse(call: Call<SendReportResponse>?, response: Response<SendReportResponse>?) {
						response?.let {
							if (it.isSuccessful) {
								
								if (it.body() != null) {
									if (it.body() != null) {
										sendReportCallback.onSuccess()
									} else {
										sendReportCallback.onFailed(null, context.getString(R.string.error_common))
									}
								}
								
							} else {
								
								if (response.code() == 401) {
									sendReportCallback.onFailed(TokenExpireException(context), null)
									return
								}
								
								if (response.errorBody() != null) {
									try {
										val error: ErrorResponse = GsonProvider.getInstance().gson.fromJson(response.errorBody()?.string(), ErrorResponse::class.java)
										sendReportCallback.onFailed(null, error.message)
									} catch (e: Exception) {
										sendReportCallback.onFailed(null, context.getString(R.string.error_common))
									}
								} else {
									sendReportCallback.onFailed(null, context.getString(R.string.error_common))
								}
							}
							
						}
					}
					
				})
		
	}
	
	
	interface SendReportCallback {
		fun onSuccess()
		fun onFailed(t: Throwable?, message: String?)
	}
}