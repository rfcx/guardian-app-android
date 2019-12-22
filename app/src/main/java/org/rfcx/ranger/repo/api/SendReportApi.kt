package org.rfcx.ranger.repo.api

import android.content.Context
import android.net.Uri
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Result
import org.rfcx.ranger.entity.report.Report
import org.rfcx.ranger.entity.report.SendReportResponse
import org.rfcx.ranger.repo.ApiCallback
import org.rfcx.ranger.repo.ApiManager
import org.rfcx.ranger.repo.responseParser
import org.rfcx.ranger.util.getTokenID
import org.rfcx.ranger.util.toIsoString
import retrofit2.Call
import retrofit2.Response
import java.io.File

class SendReportApi {
	
	val tag = "SendReportApi"
	
	fun sendSync(context: Context, report: Report): Result<SendReportResponse, Exception> {
		
		val response: Response<SendReportResponse>?
		try {
			response = request(context, report).execute()
		} catch (e: Exception) {
			return Err(e)
		}
		
		return responseParser(response)
	}
	
	private fun request(context: Context, report: Report): Call<SendReportResponse> {
		val token = context.getTokenID() ?: throw Exception("Null token")
		
		val authUser = "Bearer $token"
		val audioFileOrNull = if (!report.audioLocation.isNullOrEmpty()) createLocalFilePart("audio", Uri.parse(report.audioLocation!!), "audio/mpeg") else null
		val notes = if (!report.notes.isNullOrEmpty()) createPartFromString(report.notes!!) else null
		
		return if (report.guid != null) {
			ApiManager.getInstance().apiRest.updateReport(authUser = authUser, guid = report.guid!!, value = createPartFromString(report.value),
					site = createPartFromString(report.site), reportedAt = createPartFromString(report.reportedAt.toIsoString()),
					latitude = createPartFromString(report.latitude.toString()), longitude = createPartFromString(report.longitude.toString()),
					notes = notes, ageEstimate = createPartFromString(report.ageEstimateRaw.toString()), audioFile = audioFileOrNull)
		} else {
			ApiManager.getInstance().apiRest.sendReport(authUser = authUser, value = createPartFromString(report.value),
					site = createPartFromString(report.site), reportedAt = createPartFromString(report.reportedAt.toIsoString()),
					latitude = createPartFromString(report.latitude.toString()), longitude = createPartFromString(report.longitude.toString()),
					notes = notes, ageEstimate = createPartFromString(report.ageEstimateRaw.toString()), audioFile = audioFileOrNull)
		}
	}
	
	private fun createPartFromString(descriptionString: String): RequestBody {
		return RequestBody.create(okhttp3.MultipartBody.FORM, descriptionString)
	}
	
	private fun createLocalFilePart(partName: String, fileUri: Uri, mediaType: String): MultipartBody.Part {
		val file = File(fileUri.path)
		val requestFile = RequestBody.create(MediaType.parse(mediaType), file)
		return MultipartBody.Part.createFormData(partName, file.name, requestFile)
	}
	
	
	interface SendReportCallback : ApiCallback {
		fun onSuccess()
	}
}