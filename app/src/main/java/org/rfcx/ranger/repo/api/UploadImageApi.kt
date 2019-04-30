package org.rfcx.ranger.repo.api

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Result
import org.rfcx.ranger.repo.ApiManager
import org.rfcx.ranger.repo.responseParser
import org.rfcx.ranger.util.DateHelper
import org.rfcx.ranger.util.getTokenID
import retrofit2.Response
import java.io.File
import id.zelory.compressor.Compressor
import org.rfcx.ranger.entity.report.UploadImageResponse

class UploadImageApi {
	
	fun sendSync(context: Context, reportGuID: String, images: List<String>): Result<List<UploadImageResponse>, Exception> {
		
		val token = context.getTokenID() ?: return Err(Exception("Null token"))
		val authUser = "Bearer $token"
		val type = RequestBody.create(MultipartBody.FORM, "image")
		val time = RequestBody.create(MultipartBody.FORM, DateHelper.getIsoTime())
		val attachments = arrayListOf<MultipartBody.Part>()
		
		val compressedList = arrayListOf<File>()
		for (image in images) {
			compressedList.add(compressFile(context, File(Uri.parse(image).path)))
		}
		
		for (file in compressedList) {
			attachments.add(createLocalFilePart(file, "image/*"))
		}
		
		val response: Response<List<UploadImageResponse>>?
		try {
			Log.d("UploadImageApi", "Do try")
			response = ApiManager.getInstance().apiRest.uploadImages(authUser, reportGuID, type, time, attachments).execute()
		} catch (e: Exception) {
			e.printStackTrace()
			Log.d("UploadImageApi", e.message)
			return Err(e)
		}
		
		return responseParser(response)
	}
	
	private fun createLocalFilePart(file: File, mediaType: String): MultipartBody.Part {
		val requestFile = RequestBody.create(MediaType.parse(mediaType), file)
		return MultipartBody.Part.createFormData("attachments", file.name, requestFile)
	}
	
	/**
	 * compress imagePath to less than 1 MB
	 */
	private fun compressFile(context: Context?, file: File): File {
		
		if (file.length() <= 0) {
			return file
		}
		val compressed = Compressor(context)
				.setQuality(75)
				.setCompressFormat(Bitmap.CompressFormat.JPEG).compressToFile(file)
		if (compressed.length() > 1_000_000) {
			return compressFile(context, compressed)
		}
		return compressed
	}
	
	
}