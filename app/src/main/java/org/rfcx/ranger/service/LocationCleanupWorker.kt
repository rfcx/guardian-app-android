package org.rfcx.ranger.service

import android.content.Context
import android.util.Log
import androidx.work.*
import org.rfcx.ranger.localdb.LocationDb
import org.rfcx.ranger.localdb.ReportDb
import java.io.File
import java.util.concurrent.TimeUnit


/**
 * Background task for tidying up the database after location syncing
 */

class LocationCleanupWorker(context: Context, params: WorkerParameters)
    : Worker(context, params) {

    override fun doWork(): Result {
        // Delete locations older than 72 hours
        val count = LocationDb().deleteSynced()
        Log.d(TAG, "doWork: $count for deletion")

        return Result.SUCCESS
    }

    companion object {
        private val TAG = "LocationCleanupWorker"
        private val UNIQUE_WORK_KEY = "LocationCleanupWorkerUniqueKey"

        fun enqueuePeriodically() {
            val workRequest = PeriodicWorkRequestBuilder<LocationCleanupWorker>(1, TimeUnit.HOURS).build()
            WorkManager.getInstance().enqueueUniquePeriodicWork(UNIQUE_WORK_KEY, ExistingPeriodicWorkPolicy.REPLACE, workRequest)
        }
    }
}