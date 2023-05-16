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
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.data.local.deploy.DeploymentDb
import org.rfcx.incidents.data.local.realm.AppRealm
import org.rfcx.incidents.data.remote.common.service.ServiceFactory
import org.rfcx.incidents.entity.guardian.deployment.EditDeploymentRequest
import org.rfcx.incidents.entity.guardian.deployment.toRequestBody

class DeploymentSyncWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val service = ServiceFactory.makeDeploymentService(BuildConfig.DEBUG, context)
        val db = DeploymentDb(Realm.getInstance(AppRealm.configuration()))
        val deployments = db.lockUnsent()
        Log.d(TAG, "doWork: found ${deployments.size} unsent")
        var someFailed = false

        deployments.forEach { dp ->
            if (dp.externalId != null) {
                val streamRequest = dp.stream!!.toRequestBody()
                val result = service.editDeployment(dp.externalId!!, EditDeploymentRequest(streamRequest)).execute()
                if (result.isSuccessful) {
                    db.markSent(dp.externalId!!, dp.id)
                } else {
                    db.markUnsent(dp.id)
                    someFailed = true
                }
            } else {
                val deploymentRequest = dp.toRequestBody()
                val result = service.createDeployment(deploymentRequest).execute()
                val error = result.errorBody()?.string()
                when {
                    result.isSuccessful -> {
                        val fullId = result.headers()["Location"]
                        val id = fullId?.substring(fullId.lastIndexOf("/") + 1, fullId.length) ?: ""
                        db.markSent(id, dp.id)
                    }
                    error?.contains("this deploymentKey is already existed") ?: false -> {
                        db.markSent(dp.deploymentKey, dp.id)
                    }
                    else -> {
                        db.markUnsent(dp.id)
                        someFailed = true
                    }
                }
            }
        }
        ImageSyncWorker.enqueue()
        return if (someFailed) Result.retry() else Result.success()
    }

    companion object {
        private const val TAG = "DeploymentSyncWorker"
        private const val UNIQUE_WORK_KEY = "DeploymentSyncWorkerUniqueKey"

        fun enqueue() {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val workRequest = OneTimeWorkRequestBuilder<DeploymentSyncWorker>().setConstraints(constraints).build()
            WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_KEY, ExistingWorkPolicy.KEEP, workRequest)
        }

        fun workInfos(): LiveData<List<WorkInfo>> {
            return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
        }
    }
}
