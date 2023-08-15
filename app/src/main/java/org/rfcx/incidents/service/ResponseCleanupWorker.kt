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
import org.rfcx.incidents.data.local.deploy.DeploymentDb
import org.rfcx.incidents.data.local.deploy.DeploymentImageDb
import org.rfcx.incidents.data.local.guardian.GuardianRegistrationDb
import org.rfcx.incidents.data.local.realm.AppRealm
import org.rfcx.incidents.service.deploy.DeploymentSyncWorker
import org.rfcx.incidents.service.deploy.RegistrationSyncWorker
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

        val deploymentDb = DeploymentDb(realm)
        val deploymentUnsent = deploymentDb.unsentCount()
        deploymentDb.unlockSending()
        if (deploymentUnsent > 0) {
            DeploymentSyncWorker.enqueue()
        }

        val registrationDb = GuardianRegistrationDb(realm)
        val registerUnsent = registrationDb.unsentCount()
        registrationDb.unlockSending()
        if (registerUnsent > 0) {
            RegistrationSyncWorker.enqueue()
        }

        val imageDb = DeploymentImageDb(realm)
        val imageUnsent = imageDb.unsentCount()
        imageDb.unlockSending()
        if (imageUnsent > 0) {
            // ImageSyncWorker.enqueue()
        }
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
