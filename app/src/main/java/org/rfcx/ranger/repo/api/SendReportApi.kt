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
		val audioFileOrNull = if (!report.audioLocation.isNullOrEmpty()) createLocalFilePart("audio", Uri.parse(report.audioLocation!!), "audio/mpeg") else null

		ApiManager.getInstance().apiRest.sendReport(authUser =  authUser, value = createPartFromString(report.value),
				site = createPartFromString(report.site), reportedAt = createPartFromString(report.reportedAt),
				latitude = createPartFromString(report.latitude.toString()), longitude = createPartFromString(report.longitude.toString()),
				ageEstimate = createPartFromString(report.ageEstimate.toString()), audioFile = audioFileOrNull)
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