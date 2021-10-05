package org.rfcx.ranger.service

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import io.realm.Realm
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.data.remote.service.ServiceFactory
import org.rfcx.ranger.entity.response.toCreateResponseRequest
import org.rfcx.ranger.localdb.ResponseDb
import org.rfcx.ranger.util.RealmHelper


/**
 * Background task for syncing data to the server
 */

class ResponseSyncWorker(private val context: Context, params: WorkerParameters)
	: Worker(context, params) {
	
	override fun doWork(): Result {
		Log.d(TAG, "doWork")
		
		val eventService = ServiceFactory.makeCreateResponseService(BuildConfig.DEBUG, context)
		val db = ResponseDb(Realm.getInstance(RealmHelper.migrationConfig()))
		val responses = db.lockUnsent()
		Log.d(TAG, "doWork: found ${responses.size} unsent")
		
		var someFailed = false
		for (response in responses) {
			val result = eventService.createNewResponse(response.toCreateResponseRequest()).execute()
			if (result.isSuccessful) {
				val incidentRef = result.body()?.incidentRef
				val responseId = result.headers().toString().split("/").last()
				db.markSent(response.id, responseId, incidentRef)
			} else {
				someFailed = true
				db.markUnsent(response.id)
			}
		}
		
		// upload attaches image
		// ImageUploadWorker.enqueue()
		
		return if (someFailed) Result.retry() else Result.success()
	}
	
	companion object {
		private const val TAG = "ResponseSyncWorker"
		private const val UNIQUE_WORK_KEY = "ResponseSyncWorkerUniqueKey"
		
		fun enqueue() {
			val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
			val workRequest = OneTimeWorkRequestBuilder<ResponseSyncWorker>().setConstraints(constraints).build()
			WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_KEY, ExistingWorkPolicy.REPLACE, workRequest)
		}
		
		fun workInfos(): LiveData<List<WorkInfo>> {
			return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
		}
	}
}
