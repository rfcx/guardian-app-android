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
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.local.deploy.DeploymentDb
import org.rfcx.incidents.data.local.realm.AppRealm
import org.rfcx.incidents.data.remote.common.service.ServiceFactory
import org.rfcx.incidents.entity.guardian.deployment.EditDeploymentRequest
import org.rfcx.incidents.entity.guardian.deployment.toDeploymentRequestBody
import org.rfcx.incidents.entity.guardian.deployment.toRequestBody
import org.rfcx.incidents.entity.response.SyncState

class DeploymentSyncWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val service = ServiceFactory.makeDeploymentService(BuildConfig.DEBUG, context)
        val streamDb = StreamDb(Realm.getInstance(AppRealm.configuration()))
        val deploymentDb = DeploymentDb(Realm.getInstance(AppRealm.configuration()))
        val streams = streamDb.getAllForWorker().filter { it.deployment != null && it.deployment!!.syncState == SyncState.UNSENT.value}

        Log.d(TAG, "doWork: found ${streams.size} unsent")
        var someFailed = false

        streams.forEach { stream ->
            stream.deployment?.let { dp ->
                if (dp.externalId != null) {
                    val streamRequest = stream.toRequestBody()
                    val result = service.editDeployment(dp.externalId!!, EditDeploymentRequest(streamRequest)).execute()
                    if (result.isSuccessful) {
                        deploymentDb.markSent(dp.externalId!!, dp.id)
                    } else {
                        deploymentDb.markUnsent(dp.id)
                        someFailed = true
                    }
                } else {
                    val deploymentRequest = stream.toDeploymentRequestBody()
                    val result = service.createDeployment(deploymentRequest).execute()
                    val error = result.errorBody()?.string()
                    when {
                        result.isSuccessful -> {
                            val fullId = result.headers()["Location"]
                            val id = fullId?.substring(fullId.lastIndexOf("/") + 1, fullId.length) ?: ""
                            deploymentDb.markSent(id, dp.id)

                            val updatedDp = service.getDeployment(id).execute()
                            if (updatedDp.isSuccessful) {
                                updatedDp.body()?.let {
                                    streamDb.updateSiteServerId(stream, it.stream!!.id)
                                }
                            }
                        }
                        error?.contains("this deploymentKey is already existed") ?: false -> {
                            deploymentDb.markSent(dp.deploymentKey, dp.id)

                            val updatedDp = service.getDeployment(dp.deploymentKey).execute()
                            if (updatedDp.isSuccessful) {
                                updatedDp.body()?.let {
                                    streamDb.updateSiteServerId(stream, it.stream!!.id)
                                }
                            }
                        }
                        else -> {
                            deploymentDb.markUnsent(dp.id)
                            someFailed = true
                        }
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
            WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_KEY, ExistingWorkPolicy.REPLACE, workRequest)
        }

        fun workInfos(): LiveData<List<WorkInfo>> {
            return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
        }
    }
}
