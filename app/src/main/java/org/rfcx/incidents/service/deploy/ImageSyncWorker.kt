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
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.realm.Realm
import me.echodev.resizer.Resizer
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.data.local.deploy.DeploymentDb
import org.rfcx.incidents.data.local.deploy.DeploymentImageDb
import org.rfcx.incidents.data.local.realm.AppRealm
import org.rfcx.incidents.data.remote.common.service.ServiceFactory
import org.rfcx.incidents.util.FileUtils.getMimeType
import java.io.File

class ImageSyncWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val service = ServiceFactory.makeDeploymentService(BuildConfig.DEBUG, context)
        val db = DeploymentDb(Realm.getInstance(AppRealm.configuration()))
        val imageDb = DeploymentImageDb(Realm.getInstance(AppRealm.configuration()))
        val images = db.get().filter { it.externalId != null }.map { dp ->
            Pair(dp.externalId, dp.images?.filter { it.remotePath == null }?.toList())
        }

        Log.d(TAG, "doWork: found ${images.size} unsent")
        var someFailed = false

        images.forEach {
            if (it.first != null && it.second != null) {
                it.second?.forEach { image ->
                    val file = File(image.localPath)
                    val mimeType = file.getMimeType()
                    val requestFile = RequestBody.create(mimeType.toMediaTypeOrNull(), compressFile(context, file))
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                    val gson = Gson()
                    val obj = JsonObject()
                    obj.addProperty("label", image.imageLabel)
                    val label = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), gson.toJson(obj))

                    imageDb.lockUnsent(image.id)

                    val result = service.uploadImage(it.first!!, body, label).execute()
                    if (result.isSuccessful) {
                        val assetPath = result.headers()["Location"]
                        assetPath?.let { path ->
                            imageDb.markSent(image.id, path.substring(1, path.length))
                        }
                    } else {
                        imageDb.markUnsent(image.id)
                        someFailed = true
                    }
                }
            }
        }

        return if (someFailed) Result.retry() else Result.success()
    }

    private fun compressFile(context: Context?, file: File): File {
        if (file.length() <= 0) {
            return file
        }
        return Resizer(context)
            .setTargetLength(1920)
            .setQuality(80)
            .setSourceImage(file)
            .resizedFile
    }

    companion object {
        private const val TAG = "ImageSyncWorker"
        private const val UNIQUE_WORK_KEY = "ImageSyncWorkerUniqueKey"

        fun enqueue() {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val workRequest = OneTimeWorkRequestBuilder<ImageSyncWorker>().setConstraints(constraints).build()
            WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_KEY, ExistingWorkPolicy.KEEP, workRequest)
        }

        fun workInfos(): LiveData<List<WorkInfo>> {
            return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
        }
    }
}
