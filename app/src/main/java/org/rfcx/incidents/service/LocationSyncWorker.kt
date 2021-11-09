package org.rfcx.incidents.service

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import io.realm.Realm
import org.rfcx.incidents.entity.Err
import org.rfcx.incidents.entity.Ok
import org.rfcx.incidents.localdb.LocationDb
import org.rfcx.incidents.repo.api.SendLocationApi
import org.rfcx.incidents.util.RealmHelper


/**
 * Background task for syncing the location tracking data to the server
 */

class LocationSyncWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        Log.d(TAG, "doWork")

        val api = SendLocationApi()
        val db = LocationDb(Realm.getInstance(RealmHelper.migrationConfig()))
        var checkins = db.unsent()

        if (checkins.isEmpty()) {
            return Result.success()
        }

        Log.d(TAG, "doWork: found ${checkins.size} unsent")

        // When there is a lot of checkins (e.g. when the app has been offline for long periods)
        // then only upload the first X checkins and requeue the job
        if (checkins.size > MAXIMUM_BATCH_SIZE) {
            checkins = checkins.subList(0, MAXIMUM_BATCH_SIZE)
            enqueue()
        }

        Log.d(TAG, "doWork: sending ${checkins.size}")
        val checkinIds = checkins.map { it.id }

        val result = api.sendSync(applicationContext, checkins)
        when (result) {
            is Ok -> {
                Log.d(TAG, "doWork: success")
                db.markSent(checkinIds)
                return Result.success()
            }
            is Err -> {
                Log.d(TAG, "doWork: failed")
                return Result.retry()
            }
        }
    }

    companion object {
        private const val TAG = "LocationSyncWorker"
        private const val UNIQUE_WORK_KEY = "LocationSyncWorkerUniqueKey"
        private const val MAXIMUM_BATCH_SIZE = 100

        fun enqueue() {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val workRequest = OneTimeWorkRequestBuilder<LocationSyncWorker>().setConstraints(constraints).build()
            WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_KEY, ExistingWorkPolicy.REPLACE, workRequest)
        }

        fun workInfos(): LiveData<List<WorkInfo>> {
            return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
        }
    }
}