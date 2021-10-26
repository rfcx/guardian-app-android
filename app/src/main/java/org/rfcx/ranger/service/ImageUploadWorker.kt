package org.rfcx.ranger.service

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.*
import io.realm.Realm
import me.echodev.resizer.Resizer
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.data.remote.service.ServiceFactory
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.util.RealmHelper
import java.io.File


/**
 * Background task for syncing data to the server
 */

class ImageUploadWorker(private val context: Context, params: WorkerParameters)
	: Worker(context, params) {
	
	override fun doWork(): Result {
		val api = ServiceFactory.makeAssetsService(BuildConfig.DEBUG, context)
		val db = ReportImageDb(Realm.getInstance(RealmHelper.migrationConfig()))
		val images = db.lockUnsent()
		
		var someFailed = false
		for (image in images) {
			
			val imageFile = File(image.localPath)
			if (!imageFile.exists()) {
				return Result.failure()
			}
			
			val compressedFile = compressFile(context, imageFile)
			val file = if (imageFile.length() < compressedFile.length()) {
				createLocalFilePart(imageFile, "image/*")
			} else {
				createLocalFilePart(compressedFile, "image/*")
			}
			
			image.reportServerId?.let {
				val result = api.uploadAssets(it, file).execute()
				if (result.isSuccessful) {
					val assetPath = result.headers().get("Location")
					assetPath?.let { path ->
						db.markSent(image.id, path.substring(1, path.length))
					}
				} else {
					db.markUnsent(image.id)
					someFailed = true
				}
			}
		}
		
		return if (someFailed) Result.retry() else Result.success()
	}
	
	private fun createLocalFilePart(file: File, mediaType: String): MultipartBody.Part {
		val requestFile = RequestBody.create(mediaType.toMediaTypeOrNull(), file)
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
