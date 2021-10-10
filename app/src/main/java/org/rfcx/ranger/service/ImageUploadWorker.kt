package org.rfcx.ranger.service

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import io.realm.Realm
import me.echodev.resizer.Resizer
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.data.remote.service.ServiceFactory
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.util.RealmHelper
import retrofit2.Response
import java.io.File


/**
 * Background task for syncing data to the server
 */

class ImageUploadWorker(private val context: Context, params: WorkerParameters)
	: Worker(context, params) {
	
	override fun doWork(): Result {
		Log.d(TAG, "doWork")
		
		val api = ServiceFactory.makeAssetsService(BuildConfig.DEBUG, context)
		val db = ReportImageDb(Realm.getInstance(RealmHelper.migrationConfig()))
		val images = db.lockUnsent()
		
		Log.d(TAG, "doWork images ${images.size}")
		
		var someFailed = false
		for (image in images) {
			
			val file: MultipartBody.Part
			val imageFile = File(image.localPath)
			if (!imageFile.exists()) {
				return Result.failure()
			}
			
			val compressedFile = compressFile(context, imageFile)
			file = if (imageFile.length() < compressedFile.length()) {
				createLocalFilePart(imageFile, "image/*")
			} else {
				createLocalFilePart(compressedFile, "image/*")
			}
			
			val result: Response<ResponseBody>?
			image.reportServerId?.let {
				result = api.uploadAssets(it, file).execute()
				if (result.isSuccessful) {
					val remotePath = result.headers().toString().split("/").last()
					db.markSent(image.id, remotePath)
				} else {
					db.markUnsent(image.id)
					someFailed = true
				}
			}
		}
		
		return if (someFailed) Result.retry() else Result.success()
	}
	
	private fun createLocalFilePart(file: File, mediaType: String): MultipartBody.Part {
		val requestFile = RequestBody.create(MediaType.parse(mediaType), file)
		return MultipartBody.Part.createFormData("file", file.name, requestFile)
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
	
	companion object {
		private const val TAG = "ImageUploadWorker"
		private const val UNIQUE_WORK_KEY = "ImageUploadWorkerUniqueKey"
		
		fun enqueue() {
			val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
			val workRequest = OneTimeWorkRequestBuilder<ImageUploadWorker>().setConstraints(constraints).build()
			WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_KEY, ExistingWorkPolicy.KEEP, workRequest)
		}
		
		fun workInfos(): LiveData<List<WorkInfo>> {
			return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
		}
	}
}
