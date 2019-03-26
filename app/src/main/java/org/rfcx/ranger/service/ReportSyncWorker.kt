package org.rfcx.ranger.service

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.repo.api.SendReportApi
import java.io.File


/**
 * Background task for syncing data to the server
 */

class ReportSyncWorker(context: Context, params: WorkerParameters)
    : Worker(context, params) {

    override fun doWork(): Result {
        Log.d(TAG, "doWork")

        val api = SendReportApi()
        val db = ReportDb()
        val reports = db.lockUnsent()

        Log.d(TAG, "doWork: found ${reports.size} unsent")

        var someFailed = false

        for (report in reports) {
            Log.d(TAG, "doWork: sending ${report.id}")
            val result = api.sendSync(applicationContext, report)
            when (result) {
                is Ok -> {
                    val guid = result.value.guid
                    Log.d(TAG, "doWork: success ${report.id}")
                    db.markSent(report.id, guid)
                }
                is Err -> {
                    Log.d(TAG, "doWork: failed ${report.id}")
                    db.markUnsent(report.id)
                    someFailed = true
                }
            }
        }

//        deleteSentReports()

        return if (someFailed) Result.retry() else Result.success()
    }

    private fun deleteSentReports() {
        val db = ReportDb()
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

    companion object {
        private const val TAG = "ReportSyncWorker"
        private const val UNIQUE_WORK_KEY = "ReportSyncWorkerUniqueKey"

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