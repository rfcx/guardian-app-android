package org.rfcx.ranger.service

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import com.google.common.util.concurrent.ListenableFuture
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.repo.api.SendReportApi
import org.rfcx.ranger.localdb.ReportDb
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
                    Log.d(TAG, "doWork: success ${report.id}")
                    db.markSent(report.id)
                }
                is Err -> {
                    Log.d(TAG, "doWork: failed ${report.id}")
                    db.markUnsent(report.id)
                    someFailed = true
                }
            }
        }

        deleteSentReports()

        return if (someFailed) Result.RETRY else Result.SUCCESS
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
        private val TAG = "ReportSyncWorker"
        private val UNIQUE_WORK_KEY = "ReportSyncWorkerUniqueKey"

        fun enqueue() {
            val workRequest = OneTimeWorkRequestBuilder<ReportSyncWorker>().build()
            WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_KEY, ExistingWorkPolicy.APPEND, workRequest)
        }

        fun workInfos(): LiveData<List<WorkInfo>> {
            return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
        }
    }
}