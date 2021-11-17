package org.rfcx.incidents.service

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import io.realm.Realm
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.data.local.EventDb
import org.rfcx.incidents.data.remote.service.ServiceFactory
import org.rfcx.incidents.entity.event.EventReview
import org.rfcx.incidents.entity.event.ReviewEventRequest
import org.rfcx.incidents.util.RealmHelper


/**
 * Background task for syncing data to the server
 */

class ReviewEventSyncWorker(private val context: Context, params: WorkerParameters)
	: Worker(context, params) {
	
	override fun doWork(): Result {
		Log.d(TAG, "doWork")
		
		val eventService = ServiceFactory.makeEventService(BuildConfig.DEBUG, context)
		val db = EventDb(Realm.getInstance(RealmHelper.migrationConfig()))
		
		val reviewEvents = db.lockReviewEventUnSent()
		
		Log.d(TAG, "doWork: found ${reviewEvents.size} unsent")
		
		var someFailed = false
		
		for (reviewEvent in reviewEvents) {
			Log.d(TAG, "doWork: sending ${reviewEvent.eventGuId}")
			
			val arrayWindow = ArrayList<String>()
			val request = ReviewEventRequest(reviewEvent.review == "confirm", true, arrayWindow)
			val result = eventService.reviewEvent(reviewEvent.eventGuId, request).execute()
			
			if (result.isSuccessful) {
				db.markReviewEventSyncState(reviewEvent.eventGuId, EventReview.SENT)
			} else {
				someFailed = true
				db.markReviewEventSyncState(reviewEvent.eventGuId, EventReview.UNSENT)
			}
		}
		
		return if (someFailed) Result.retry() else Result.success()
	}
	
	companion object {
		private const val TAG = "ReviewEventSyncWorker"
		private const val UNIQUE_WORK_KEY = "ReviewEventSyncWorkerUniqueKey"
		
		fun enqueue() {
			val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
			val workRequest = OneTimeWorkRequestBuilder<ReviewEventSyncWorker>().setConstraints(constraints).build()
			WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_KEY, ExistingWorkPolicy.REPLACE, workRequest)
		}
		
		fun workInfos(): LiveData<List<WorkInfo>> {
			return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
		}
	}
}
