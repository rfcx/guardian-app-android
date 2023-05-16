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
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.local.deploy.DeploymentDb
import org.rfcx.incidents.data.local.realm.AppRealm

class DeploymentSyncWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val db = DeploymentDb(Realm.getInstance(AppRealm.configuration()))
        val deployments = db.get()
        val streamDb = StreamDb(Realm.getInstance(AppRealm.configuration()))
        val stream = streamDb.getByProject("3dvrocmagfiw")
        Log.d(TAG, "doWork: found ${deployments.size} unsent and ${stream.size} all")
        return Result.success()
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
