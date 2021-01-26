package org.rfcx.ranger.repo.api

import android.content.Context
import android.util.Log
import me.echodev.resizer.Resizer
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Result
import org.rfcx.ranger.entity.report.ReportImage
import org.rfcx.ranger.entity.report.UploadImageResponse
import org.rfcx.ranger.repo.ApiManager
import org.rfcx.ranger.repo.responseParser
import org.rfcx.ranger.util.getTokenID
import org.rfcx.ranger.util.toIsoString
import retrofit2.Response
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class UploadImageApi {
	
	fun sendSync(context: Context, reportImage: ReportImage): Result<List<UploadImageResponse>, Exception> {
		
		val token = context.getTokenID() ?: return Err(Exception("Null token"))
		val authUser = "Bearer $token"
		val type = RequestBody.create(MultipartBody.FORM, "image")
		val time = RequestBody.create(MultipartBody.FORM, Date().toIsoString())
		val attachments = arrayListOf<MultipartBody.Part>()
		
		
		val imageFile = File(reportImage.localPath)
		
		if (!imageFile.exists()) {
			return Err(FileNotFoundException("Image attachments not found."))
		}
		
		val compressedFile = compressFile(context, imageFile)
		
		if(imageFile.length() < compressedFile.length()) {
			attachments.add(createLocalFilePart(imageFile, "image/*"))
		} else {
			attachments.add(createLocalFilePart(compressedFile, "image/*"))
		}
		
		val response: Response<List<UploadImageResponse>>?
		try {
			Log.d("UploadImageApi", "Do try")
			response = ApiManager.getInstance().apiRest.uploadImages(authUser, reportImage.guid!!, type, time, attachments).execute()
		} catch (e: Exception) {
			e.printStackTrace()
			Log.d("UploadImageApi", e.message)
			return Err(e)
		}
		
		// remove file
		try {
			compressedFile.deleteOnExit()
		} catch (ignore: Exception) {
			ignore.printStackTrace()
		}
		return responseParser(response)
	}
	
	private fun createLocalFilePart(file: File, mediaType: String): MultipartBody.Part {
		val requestFile = RequestBody.create(MediaType.parse(mediaType), file)
		return MultipartBody.Part.createFormData("attachments", file.name, requestFile)
	}
	
	private fun compressFile(context: Context?, file: File): File {
		if (file.length() <= 0) {
			return file
		}
		return Resizer(context)
				.setTargetLength(1920)
				.setQuality(80)
				.setSourceImage(file)
				.resizedFile
	}
}
