package org.rfcx.ranger.service

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.repo.api.UploadImageApi


/**
 * Background task for syncing data to the server
 */

class ImageUploadWorker(context: Context, params: WorkerParameters)
	: Worker(context, params) {
	
	override fun doWork(): Result {
		Log.d(TAG, "doWork")
		
		val api = UploadImageApi()
		val db = ReportImageDb()
		val images = db.lockUnsent()
		
		var someFailed = false
		for (image in images) {
			val result = api.sendSync(applicationContext, image)
			when (result) {
				is Ok -> {
					Log.d(TAG, "doWork: success ${image.id}")
					db.markSent(image.id)
				}
				is Err -> {
					Log.d(TAG, "doWork: failed ${image.id}")
					db.markUnsent(image.id)
					someFailed = true
				}
			}
		}
		
		return if (someFailed) Result.retry() else Result.success()
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