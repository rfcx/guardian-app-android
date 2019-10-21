package org.rfcx.ranger.service

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import io.realm.Realm
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.repo.api.SendReportApi
import org.rfcx.ranger.util.RealmHelper
import java.io.File


/**
 * Background task for syncing data to the server
 */

class ReportSyncWorker(context: Context, params: WorkerParameters)
    : Worker(context, params) {

    override fun doWork(): Result {
        Log.d(TAG, "doWork")

        val api = SendReportApi()
        val db = ReportDb(Realm.getInstance(RealmHelper.migrationConfig()))
        val reports = db.lockUnsent()

        Log.d(TAG, "doWork: found ${reports.size} unsent")

        var someFailed = false

        for (report in reports) {
            Log.d(TAG, "doWork: sending ${report.id}")
            when (val result = api.sendSync(applicationContext, report)) {
                is Ok -> {
                    val guid = result.value.guid
                    Log.d(TAG, "doWork: success ${report.id}")
                    db.markSent(report.id, guid)
                }
                is Err -> {
                    if (isErrorSiteNotFound(result.error.message ?: "")){
                        db.deleteReport(report.id) // site not found should remove report!
                    } else {
                        Log.d(TAG, "doWork: failed ${report.id}")
                        db.markUnsent(report.id)
                        someFailed = true
                    }
                }
            }
        }

        deleteSentReports()
        // upload attaches image
        ImageUploadWorker.enqueue()

        return if (someFailed) Result.retry() else Result.success()
    }

    private fun deleteSentReports() {
        val db = ReportDb(Realm.getInstance(RealmHelper.migrationConfig()))
        val leftoverFiles = db.deleteSent()

        for (filename in leftoverFiles) {
            Log.d(TAG, "deleteSentReports: $filename")
            val file = File(filename)
            if (file.exists()) {
                val result = file.delete()
                Log.d(TAG, "deleteSentReports success: $result")
            }
        }
    }

    private fun isErrorSiteNotFound(errorMessage: String): Boolean {
        return errorMessage.contains(ERROR_SITE_NOT_FOUND)
    }

    companion object {
        private const val TAG = "ReportSyncWorker"
        private const val UNIQUE_WORK_KEY = "ReportSyncWorkerUniqueKey"

        // TBD - {"message":"Site with given guid not found.","error":{"status":404}}
        private const val ERROR_SITE_NOT_FOUND = "Site with given guid not found"

        fun enqueue() {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val workRequest = OneTimeWorkRequestBuilder<ReportSyncWorker>().setConstraints(constraints).build()
            WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_KEY, ExistingWorkPolicy.REPLACE, workRequest)
        }

        fun workInfos(): LiveData<List<WorkInfo>> {
            return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
        }
    }
}