package org.rfcx.ranger.service

import android.content.Context
import android.util.Log
import androidx.work.*
import org.rfcx.ranger.repo.api.SendReportApi
import org.rfcx.ranger.util.ReportDb
import java.io.File
import java.util.concurrent.TimeUnit


/**
 * Background task for syncing data to the server
 */

class DataSyncWorker(context: Context, params: WorkerParameters)
    : Worker(context, params) {

    override fun doWork(): Result {

        deleteSentReports()
        sendReports()

        // Indicate success or failure with your return value:
        return Result.SUCCESS

        // (Returning RETRY tells WorkManager to try this task again
        // later; FAILURE says not to try again.)
    }

    private fun sendReports() {
        val api = SendReportApi()
        val db = ReportDb()
        val reports = db.lockUnsent()

        Log.d(TAG, "sendReports: found ${reports.size} unsent")

        for (report in reports) {
            Log.d(TAG, "sendReports: sending ${report.id}")
            api.send(applicationContext, report, object : SendReportApi.SendReportCallback {
                override fun onSuccess() {
                    Log.d(TAG, "sendReports: success ${report.id}")
                    db.markSent(report.id)
                }
                override fun onFailed(t: Throwable?, message: String?) {
                    Log.d(TAG, "sendReports: failed ${report.id}")
                    db.markUnsent(report.id)
                }
            })
        }

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
        private val TAG = "DataSyncWorker"

        fun startRecurring() {
            val workRequest = PeriodicWorkRequestBuilder<DataSyncWorker>(60, TimeUnit.SECONDS)

            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            workRequest.setConstraints(constraints)

            WorkManager.getInstance().enqueue(workRequest.build())
        }
    }
}