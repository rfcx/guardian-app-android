package org.rfcx.incidents.service

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import io.realm.Realm
import org.rfcx.companion.service.TrackingSyncWorker
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.data.local.AlertDb
import org.rfcx.incidents.data.remote.common.service.ServiceFactory
import org.rfcx.incidents.entity.response.toCreateResponseRequest
import org.rfcx.incidents.data.local.ReportImageDb
import org.rfcx.incidents.data.local.ResponseDb
import org.rfcx.incidents.data.local.TrackingFileDb
import org.rfcx.incidents.data.local.VoiceDb
import org.rfcx.incidents.util.RealmHelper

/**
 * Background task for syncing data to the server
 */

class ResponseSyncWorker(private val context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        Log.d(TAG, "doWork")

        val eventService = ServiceFactory.makeCreateResponseService(BuildConfig.DEBUG, context)
        val db = ResponseDb(Realm.getInstance(RealmHelper.migrationConfig()))
        val alertDb = AlertDb(Realm.getInstance(RealmHelper.migrationConfig()))
        val reportImageDb = ReportImageDb(Realm.getInstance(RealmHelper.migrationConfig()))
        val trackingFileDb = TrackingFileDb(Realm.getInstance(RealmHelper.migrationConfig()))
        val voiceDb = VoiceDb(Realm.getInstance(RealmHelper.migrationConfig()))

        val responses = db.lockUnsent()
        Log.d(TAG, "doWork: found ${responses.size} unsent")

        var someFailed = false
        for (response in responses) {
            val result = eventService.createNewResponse(response.toCreateResponseRequest()).execute()
            if (result.isSuccessful) {
                val incidentRef = result.body()?.incidentRef
                val fullId = result.headers().get("Location")
                val id = fullId?.substring(fullId.lastIndexOf("/") + 1, fullId.length)
                db.markSent(response.id, id, incidentRef)
                trackingFileDb.updateResponseServerId(response.id, id)
                TrackingSyncWorker.enqueue()

                if (id != null) {
                    reportImageDb.saveReportServerIdToImage(id, response.id)
                    voiceDb.saveReportServerId(id, response.id)
                }
                alertDb.deleteAlertsByStreamId(response.streamId)
            } else {
                someFailed = true
                db.markUnsent(response.id)
            }
        }

        // upload attaches image
        ImageUploadWorker.enqueue()

        return if (someFailed) Result.retry() else Result.success()
    }

    companion object {
        private const val TAG = "ResponseSyncWorker"
        private const val UNIQUE_WORK_KEY = "ResponseSyncWorkerUniqueKey"

        fun enqueue() {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val workRequest = OneTimeWorkRequestBuilder<ResponseSyncWorker>().setConstraints(constraints).build()
            WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_KEY, ExistingWorkPolicy.REPLACE, workRequest)
        }

        fun workInfos(): LiveData<List<WorkInfo>> {
            return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
        }
    }
}
