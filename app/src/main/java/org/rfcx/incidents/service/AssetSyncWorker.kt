package org.rfcx.incidents.service

import android.content.Context
import android.net.Uri
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
import me.echodev.resizer.Resizer
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.data.local.AssetDb
import org.rfcx.incidents.data.local.realm.AppRealm
import org.rfcx.incidents.data.remote.common.service.ServiceFactory
import org.rfcx.incidents.entity.response.AssetType
import org.rfcx.incidents.util.FileUtils.getMimeType
import java.io.File

class AssetSyncWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val assetsService = ServiceFactory.makeAssetsService(BuildConfig.DEBUG, context)
        val db = AssetDb(Realm.getInstance(AppRealm.configuration()))
        val assets = db.lockUnsent()
        var someFailed = false
        Log.d(TAG, "doWork: found ${assets.size} unsent")

        for (asset in assets) {
            asset.serverId?.let { serverId ->
                var file: MultipartBody.Part? = null
                when (asset.type) {
                    AssetType.AUDIO.value -> {
                        file = createLocalFilePart(Uri.parse(asset.localPath), mediaType = "audio/mpeg")
                    }
                    AssetType.KML.value -> {
                        file = createLocalFilePart(fileAsset = File(asset.localPath))
                    }
                    else -> {
                        val localPath =
                            if (asset.localPath.startsWith("file://")) asset.localPath.replace("file://", "") else asset.localPath

                        val imageFile = File(localPath)
                        if (!imageFile.exists()) {
                            return Result.failure()
                        }

                        val compressedFile = compressFile(context, imageFile)
                        file = if (imageFile.length() < compressedFile.length()) {
                            createLocalFilePart(mediaType = "image/*", fileAsset = imageFile)
                        } else {
                            createLocalFilePart(mediaType = "image/*", fileAsset = compressedFile)
                        }
                    }
                }

                val result = assetsService.uploadAssets(serverId, file).execute()
                if (result.isSuccessful) {
                    val assetPath = result.headers()["Location"]
                    assetPath?.let { path ->
                        db.markSent(asset.id, path.substring(1, path.length))
                    }
                } else {
                    db.markUnsent(asset.id)
                    someFailed = true
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

    private fun createLocalFilePart(fileUri: Uri? = null, mediaType: String? = null, fileAsset: File? = null): MultipartBody.Part {
        val file = fileAsset ?: File(fileUri?.path)
        val mimeType = mediaType ?: file.getMimeType()
        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("file", file.name, requestFile)
    }

    companion object {
        private const val TAG = "AssetSyncWorker"
        private const val UNIQUE_WORK_KEY = "AssetSyncWorkerUniqueKey"

        fun enqueue() {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val workRequest = OneTimeWorkRequestBuilder<AssetSyncWorker>().setConstraints(constraints).build()
            WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_KEY, ExistingWorkPolicy.KEEP, workRequest)
        }

        fun workInfos(): LiveData<List<WorkInfo>> {
            return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
        }
    }
}
