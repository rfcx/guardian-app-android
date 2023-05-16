package org.rfcx.incidents.service

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.realm.Realm
import org.rfcx.incidents.data.local.AssetDb
import org.rfcx.incidents.data.local.ResponseDb
import org.rfcx.incidents.data.local.realm.AppRealm
import org.rfcx.incidents.service.deploy.DeploymentSyncWorker
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

        val assetDb = AssetDb(realm)
        val assetUnsent = assetDb.unsentCount()
        assetDb.unlockSending()
        if (assetUnsent > 0) {
            AssetSyncWorker.enqueue()
        }

        DeploymentSyncWorker.enqueue()
    }

    companion object {
        private const val TAG = "ResponseCleanupWorker"
        private const val UNIQUE_WORK_KEY = "ReportCleanupWorkerUniqueKey"

        fun enqueuePeriodically() {
            val workRequest = PeriodicWorkRequestBuilder<ResponseCleanupWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance()
                .enqueueUniquePeriodicWork(UNIQUE_WORK_KEY, ExistingPeriodicWorkPolicy.REPLACE, workRequest)
        }
    }
}
