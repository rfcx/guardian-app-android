package org.rfcx.incidents.service

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.realm.Realm
import org.rfcx.companion.service.TrackingSyncWorker
import org.rfcx.incidents.AppRealm
import org.rfcx.incidents.data.local.ReportImageDb
import org.rfcx.incidents.data.local.ResponseDb
import org.rfcx.incidents.data.local.TrackingFileDb
import java.util.concurrent.TimeUnit

/**
 * Background task for tidying up the database and files after report syncing
 */

class ResponseCleanupWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        Log.d(TAG, "doWork")

        resendIfRequired()

        return Result.success()
    }

    private fun resendIfRequired() {
        val realm = Realm.getInstance(AppRealm.configuration())
        val responseDb = ResponseDb(realm)
        val unsent = responseDb.unsentCount()

        responseDb.unlockSending()
        if (unsent > 0) {
            ResponseSyncWorker.enqueue()
        }

        val imageDb = ReportImageDb(realm)
        val imageUnsent = imageDb.unsentCount()
        imageDb.unlockSending()
        if (imageUnsent > 0) {
            ImageUploadWorker.enqueue()
        }

        val trackingFileDb = TrackingFileDb(realm)
        val trackingFileUnsent = trackingFileDb.unsentCount()
        trackingFileDb.unlockSending()
        if (trackingFileUnsent > 0) {
            TrackingSyncWorker.enqueue()
        }
    }

    companion object {
        private const val TAG = "ReportCleanupWorker"
        private const val UNIQUE_WORK_KEY = "ReportCleanupWorkerUniqueKey"

        fun enqueuePeriodically() {
            val workRequest = PeriodicWorkRequestBuilder<ResponseCleanupWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance()
                .enqueueUniquePeriodicWork(UNIQUE_WORK_KEY, ExistingPeriodicWorkPolicy.REPLACE, workRequest)
        }
    }
}
