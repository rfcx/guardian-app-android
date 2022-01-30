package org.rfcx.incidents.repo.api

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.rfcx.incidents.entity.Err
import org.rfcx.incidents.entity.Result
import org.rfcx.incidents.entity.report.Report
import org.rfcx.incidents.entity.report.SendReportResponse
import org.rfcx.incidents.repo.ApiCallback
import org.rfcx.incidents.repo.ApiManager
import org.rfcx.incidents.repo.responseParser
import org.rfcx.incidents.util.getTokenID
import org.rfcx.incidents.util.toIsoString
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
        val audioFileOrNull = if (!report.audioLocation.isNullOrEmpty()) createLocalFilePart(
            "audio",
            Uri.parse(report.audioLocation!!),
            "audio/mpeg"
        ) else null
        val notes = if (!report.notes.isNullOrEmpty()) createPartFromString(report.notes!!) else null
        
        return if (report.guid != null) {
            ApiManager.getInstance().apiRest.updateReport(
                authUser = authUser,
                guid = report.guid!!,
                value = createPartFromString(report.value),
                site = createPartFromString(report.site),
                reportedAt = createPartFromString(report.reportedAt.toIsoString()),
                latitude = createPartFromString(report.latitude.toString()),
                longitude = createPartFromString(report.longitude.toString()),
                notes = notes,
                ageEstimate = createPartFromString(report.ageEstimateRaw.toString()),
                audioFile = audioFileOrNull
            )
        } else {
            ApiManager.getInstance().apiRest.sendReport(
                authUser = authUser,
                value = createPartFromString(report.value),
                site = createPartFromString(report.site),
                reportedAt = createPartFromString(report.reportedAt.toIsoString()),
                latitude = createPartFromString(report.latitude.toString()),
                longitude = createPartFromString(report.longitude.toString()),
                notes = notes,
                ageEstimate = createPartFromString(report.ageEstimateRaw.toString()),
                audioFile = audioFileOrNull
            )
        }
    }
    
    private fun createPartFromString(descriptionString: String): RequestBody {
        return descriptionString.toRequestBody(okhttp3.MultipartBody.FORM)
    }
    
    private fun createLocalFilePart(partName: String, fileUri: Uri, mediaType: String): MultipartBody.Part {
        val file = File(fileUri.path)
        val requestFile = file.asRequestBody(mediaType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }
    
    
    interface SendReportCallback : ApiCallback {
        fun onSuccess()
    }
}
