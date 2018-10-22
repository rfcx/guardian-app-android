package org.rfcx.ranger.repo.api

import android.content.Context
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.ErrorResponse
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.entity.report.SendReportResponse
import org.rfcx.ranger.repo.ApiManager
import org.rfcx.ranger.repo.TokenExpireException
import org.rfcx.ranger.util.GsonProvider
import org.rfcx.ranger.util.getEmail
import org.rfcx.ranger.util.getTokenID
import org.rfcx.ranger.util.getUserGuId
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