package org.rfcx.ranger.service

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.localdb.LocationDb
import org.rfcx.ranger.repo.api.SendLocationApi


/**
 * Background task for syncing the location tracking data to the server
 */

class LocationSyncWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        Log.d(TAG, "doWork")

        val api = SendLocationApi()
        val db = LocationDb()
        var checkins = db.unsent()

        if (checkins.isEmpty()) {
            return Result.success()
        }

        Log.d(TAG, "doWork: found ${checkins.size} unsent")

        var jobResult = Result.success()
        if (checkins.size > MAXIMUM_BATCH_SIZE) {
            checkins = checkins.subList(0, MAXIMUM_BATCH_SIZE)
            jobResult = Result.retry()
        }

        Log.d(TAG, "doWork: sending ${checkins.size}")
        val checkinIds = checkins.map { it.id }

        val result = api.sendSync(applicationContext, checkins)
        when (result) {
            is Ok -> {
                Log.d(TAG, "doWork: success")
                db.markSent(checkinIds)
                return jobResult
            }
            is Err -> {
                Log.d(TAG, "doWork: failed")
            }
        }

        return Result.retry()
    }

    companion object {
        private const val TAG = "LocationSyncWorker"
        private const val UNIQUE_WORK_KEY = "LocationSyncWorkerUniqueKey"
        private const val MAXIMUM_BATCH_SIZE = 100

        fun enqueue() {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val workRequest = OneTimeWorkRequestBuilder<LocationSyncWorker>().setConstraints(constraints).build()
            WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_KEY, ExistingWorkPolicy.KEEP, workRequest)
        }

        fun workInfos(): LiveData<List<WorkInfo>> {
            return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
        }
    }
}