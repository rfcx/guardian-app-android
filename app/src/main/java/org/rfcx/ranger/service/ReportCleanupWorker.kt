package org.rfcx.ranger.service

import android.content.Context
import android.util.Log
import androidx.work.*
import io.realm.Realm
import org.rfcx.ranger.localdb.ReportDb
import org.rfcx.ranger.util.RealmHelper
import java.io.File
import java.util.concurrent.TimeUnit


/**
 * Background task for tidying up the database and files after report syncing
 */

class ReportCleanupWorker(context: Context, params: WorkerParameters)
	: Worker(context, params) {
	
	override fun doWork(): Result {
		Log.d(TAG, "doWork")
		
		deleteSentReports()
		resendIfRequired()
		
		return Result.success()
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
	
	private fun resendIfRequired() {
		val db = ReportDb(Realm.getInstance(RealmHelper.migrationConfig()))
		val unsent = db.unsentCount()
		Log.d(TAG, "resendIfRequired: found $unsent unsent")
		
		// In case any failed sending, we can resend
		db.unlockSending()
	}
	
	companion object {
		private const val TAG = "ReportCleanupWorker"
		private const val UNIQUE_WORK_KEY = "ReportCleanupWorkerUniqueKey"
		
		fun enqueuePeriodically() {
			val workRequest = PeriodicWorkRequestBuilder<ReportCleanupWorker>(15, TimeUnit.MINUTES).build()
			WorkManager.getInstance().enqueueUniquePeriodicWork(UNIQUE_WORK_KEY, ExistingPeriodicWorkPolicy.REPLACE, workRequest)
		}
	}
}
