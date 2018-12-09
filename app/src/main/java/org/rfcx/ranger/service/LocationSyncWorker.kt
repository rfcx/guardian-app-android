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
        val checkins = db.unsent()

        if (checkins.isEmpty()) {
            return Result.SUCCESS
        }

        Log.d(TAG, "doWork: found ${checkins.size} unsent and sending")
        val checkinIds = checkins.map { it.id }

        val result = api.sendSync(applicationContext, checkins)
        when (result) {
            is Ok -> {
                Log.d(TAG, "doWork: success")
                db.markSent(checkinIds)
                return Result.SUCCESS
            }
            is Err -> {
                Log.d(TAG, "doWork: failed")
            }
        }

        return Result.RETRY
    }

    companion object {
        private const val TAG = "LocationSyncWorker"
        private const val UNIQUE_WORK_KEY = "LocationSyncWorkerUniqueKey"

        fun enqueue() {
            val workRequest = OneTimeWorkRequestBuilder<LocationSyncWorker>().build()
            WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_KEY, ExistingWorkPolicy.KEEP, workRequest)
        }

        fun workInfos(): LiveData<List<WorkInfo>> {
            return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
        }
    }
}