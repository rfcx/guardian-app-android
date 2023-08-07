package org.rfcx.incidents.service.deploy

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.realm.Realm
import org.rfcx.incidents.data.local.guardian.GuardianRegistrationDb
import org.rfcx.incidents.data.local.realm.AppRealm
import org.rfcx.incidents.data.remote.common.service.ServiceFactory
import org.rfcx.incidents.entity.guardian.registration.toRequest

class RegistrationSyncWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val prodService = ServiceFactory.makeGuardianRegisterProductionService(context)
        val stagingService = ServiceFactory.makeGuardianRegisterStagingService(context)
        val db = GuardianRegistrationDb(Realm.getInstance(AppRealm.configuration()))
        val unsent = db.getAllUnsentForWorker()

        Log.d(TAG, "doWork: found ${unsent.size} unsent")
        var someFailed = false
        unsent.forEach {
            when(it.env) {
                "production" -> {
                    val result = prodService.register(it.toRequest()).execute()
                    if (result.isSuccessful) {
                        db.markSent(it.guid)
                    } else {
                        db.markUnsent(it.guid)
                        someFailed = true
                    }
                }
                "staging" -> {
                    val result = stagingService.register(it.toRequest()).execute()
                    if (result.isSuccessful) {
                        db.markSent(it.guid)
                    } else {
                        db.markUnsent(it.guid)
                        someFailed = true
                    }
                }
            }
        }

        return if (someFailed) Result.retry() else Result.success()
    }

    companion object {
        private const val TAG = "RegistrationSyncWorker"
        private const val UNIQUE_WORK_KEY = "RegistrationSyncWorkerUniqueKey"

        fun enqueue() {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val workRequest = OneTimeWorkRequestBuilder<RegistrationSyncWorker>().setConstraints(constraints).build()
            WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_KEY, ExistingWorkPolicy.KEEP, workRequest)
        }

        fun workInfos(): LiveData<List<WorkInfo>> {
            return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
        }
    }
}
