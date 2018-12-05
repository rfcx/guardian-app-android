package org.rfcx.ranger.service

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import com.google.common.util.concurrent.ListenableFuture
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.localdb.LocationDb
import org.rfcx.ranger.repo.api.SendReportApi
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.repo.api.SendLocationApi
import java.io.File


/**
 * Background task for syncing the location tracking data to the server
 */

class LocationSyncWorker(context: Context, params: WorkerParameters)
    : Worker(context, params) {

    override fun doWork(): Result {
        Log.d(TAG, "doWork")

        val api = SendLocationApi()
        val db = LocationDb()
        val checkins = db.unsent()
        val checkinIds = checkins.map { it.id }
        Log.d(TAG, "doWork: found ${checkinIds.size} unsent and sending")
		if (checkins.isEmpty()) return Result.SUCCESS
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
        private val TAG = "LocationSyncWorker"
        private val UNIQUE_WORK_KEY = "LocationSyncWorkerUniqueKey"

        fun enqueue() {
            val workRequest = OneTimeWorkRequestBuilder<LocationSyncWorker>().build()
            WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_KEY, ExistingWorkPolicy.REPLACE, workRequest)
        }

        fun workInfos(): LiveData<List<WorkInfo>> {
            return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
        }
    }
}