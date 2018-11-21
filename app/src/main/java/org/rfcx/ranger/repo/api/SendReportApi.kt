package org.rfcx.ranger.repo.api

import android.content.Context
import android.net.Uri
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.entity.report.SendReportResponse
import org.rfcx.ranger.util.getTokenID
import android.util.Log
import com.crashlytics.android.Crashlytics
import okhttp3.MediaType
import okhttp3.RequestBody
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.repo.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.MultipartBody
import java.io.File

class SendReportApi {

	val tag = "SendReportApi"
	
	fun sendReport(context: Context, report: Report, sendReportCallback: SendReportCallback) {
		
		val token = context.getTokenID()
		if (token == null) {
			sendReportCallback.onFailed(TokenExpireException(context), null)
			return
		}

		val authUser = "Bearer $token"
		
		Log.d(tag, report.toString())
		ApiManager.getInstance().apiRest.sendReport(authUser, createParts(report))
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

	private fun createParts(report: Report): Map<String, RequestBody> {
		val map = HashMap<String, RequestBody>()
		map.put("value", createPartFromString(report.value))
		map.put("site", createPartFromString(report.site))
		map.put("reported_at", createPartFromString(report.reportedAt))
		map.put("lat", createPartFromString(report.latitude.toString()))
		map.put("long", createPartFromString(report.longitude.toString()))
		map.put("age_estimate", createPartFromString(report.ageEstimate.toString()))
		if (!report.audioLocation.isNullOrEmpty()) {
			val uri = Uri.parse(report.audioLocation!!)
			map.put("audio", createLocalFilePart("audio", uri, "audio/mpeg").body())
		}
		return map
	}

	private fun createPartFromString(descriptionString: String): RequestBody {
		return RequestBody.create(okhttp3.MultipartBody.FORM, descriptionString)
	}


	private fun createLocalFilePart(partName: String, fileUri: Uri, mediaType: String): MultipartBody.Part {
		val file = File(fileUri.path)
		val requestFile = RequestBody.create(MediaType.parse(mediaType), file)
		return MultipartBody.Part.createFormData(partName, file.name, requestFile)
	}
	
	
	interface SendReportCallback: ApiCallback {
		fun onSuccess()
	}
}