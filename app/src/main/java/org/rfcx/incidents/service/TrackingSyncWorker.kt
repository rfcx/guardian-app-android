package org.rfcx.companion.service

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.realm.Realm
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.rfcx.incidents.AppRealm
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.data.local.TrackingFileDb
import org.rfcx.incidents.data.remote.common.service.ServiceFactory
import org.rfcx.incidents.util.FileUtils.getMimeType
import java.io.File

class TrackingSyncWorker(val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val db = TrackingFileDb(Realm.getInstance(AppRealm.configuration()))
        val api = ServiceFactory.makeAssetsService(BuildConfig.DEBUG, context)
        val tracking = db.lockUnsent()
        var someFailed = false

        tracking.forEach {
            val file = File(it.localPath)
            val mimeType = file.getMimeType()
            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            it.responseServerId?.let { resServerId ->
                val result = api.uploadAssets(resServerId, body).execute()
                if (result.isSuccessful) {
                    val assetPath = result.headers().get("Location")
                    assetPath?.let { path ->
                        db.markSent(it.id, path.substring(1, path.length))
                    }
                } else {
                    db.markUnsent(it.id)
                    someFailed = true
                }
            }
        }

        return if (someFailed) Result.retry() else Result.success()
    }

    companion object {
        private const val TAG = "TrackingSyncWorker"
        private const val UNIQUE_WORK_KEY = "TrackingSyncWorkerUniqueKey"

        fun enqueue() {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val workRequest = OneTimeWorkRequestBuilder<TrackingSyncWorker>().setConstraints(constraints).build()
            WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_KEY, ExistingWorkPolicy.REPLACE, workRequest)
        }

        fun workInfos(): LiveData<List<WorkInfo>> {
            return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
        }
    }
}
