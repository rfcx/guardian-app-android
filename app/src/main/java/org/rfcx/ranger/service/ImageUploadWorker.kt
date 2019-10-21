package org.rfcx.ranger.service

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import io.realm.Realm
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.localdb.ReportImageDb
import org.rfcx.ranger.repo.api.UploadImageApi
import org.rfcx.ranger.util.RealmHelper
import java.io.FileNotFoundException


/**
 * Background task for syncing data to the server
 */

class ImageUploadWorker(context: Context, params: WorkerParameters)
	: Worker(context, params) {
	
	override fun doWork(): Result {
		Log.d(TAG, "doWork")
		
		val api = UploadImageApi()
		val db = ReportImageDb(Realm.getInstance(RealmHelper.migrationConfig()))
		val images = db.lockUnsent()
		
		var someFailed = false
		for (image in images) {
			when (val result = api.sendSync(applicationContext, image)) {
				is Ok -> {
					Log.d(TAG, "doWork: success ${image.id}")
					var remotePath: String? = null
					if (result.value.isNotEmpty()) {
						remotePath = result.value[0].url
					}
					db.markSent(image.id, remotePath)
				}
				is Err -> {
					Log.d(TAG, "doWork: failed ${image.id}")
					if (result.error is FileNotFoundException) {
						// remove this attachment if file has deleted
						db.delete(image.id)
					} else {
						db.markUnsent(image.id)
						someFailed = true
					}
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