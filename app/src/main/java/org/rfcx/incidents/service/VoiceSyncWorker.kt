package org.rfcx.incidents.service

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import io.realm.Realm
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.data.remote.common.service.ServiceFactory
import org.rfcx.incidents.data.local.VoiceDb
import org.rfcx.incidents.util.RealmHelper
import java.io.File

class VoiceSyncWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val assetsService = ServiceFactory.makeAssetsService(BuildConfig.DEBUG, context)
        val db = VoiceDb(Realm.getInstance(RealmHelper.migrationConfig()))
        val voices = db.lockUnsent()
        var someFailed = false
        Log.d(TAG, "doWork: found ${voices.size} unsent")

        for (voice in voices) {
            voice.responseServerId?.let { serverId ->
                val audioFileOrNull = if (voice.localPath.isNotEmpty()) createLocalFilePart(
                    "file",
                    Uri.parse(voice.localPath),
                    "audio/mpeg"
                ) else null
                audioFileOrNull?.let { audioFile ->
                    val result = assetsService.uploadAssets(serverId, audioFile).execute()
                    if (result.isSuccessful) {
                        val assetPath = result.headers()["Location"]
                        assetPath?.let { path ->
                            db.markSent(voice.id, path.substring(1, path.length))
                        }
                    } else {
                        db.markUnsent(voice.id)
                        someFailed = true
                    }
                }
            }
        }

        return if (someFailed) Result.retry() else Result.success()
    }

    private fun createLocalFilePart(partName: String, fileUri: Uri, mediaType: String): MultipartBody.Part {
        val file = File(fileUri.path)
        val requestFile = file.asRequestBody(mediaType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    companion object {
        private const val TAG = "VoiceSyncWorker"
        private const val UNIQUE_WORK_KEY = "VoiceSyncWorkerUniqueKey"

        fun enqueue() {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val workRequest = OneTimeWorkRequestBuilder<VoiceSyncWorker>().setConstraints(constraints).build()
            WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_KEY, ExistingWorkPolicy.KEEP, workRequest)
        }

        fun workInfos(): LiveData<List<WorkInfo>> {
            return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
        }
    }
}
