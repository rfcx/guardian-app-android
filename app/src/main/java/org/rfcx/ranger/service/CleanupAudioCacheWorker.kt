package org.rfcx.ranger.service

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class CleanupAudioCacheWorker(val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
	
	private val audioDirectory = File(context.cacheDir, DownLoadEventWorker.AUDIOS_SUB_DIRECTORY)
	
	override fun doWork(): Result {
		removeAudioCache()
		removeAudioInCache(applicationContext)
		return Result.success()
	}
	
	/*
	/* for version higher or equal 1.2.6
		remove .opus all opus in 'audios' cache directory
	*/
	 */
	private fun removeAudioCache() {
		val date = Date(System.currentTimeMillis())
		audioDirectory.listFiles()?.forEach {
			if (it.isFile
					&& (it.name.substring(it.name.lastIndexOf(".") + 1) == "opus")
					&& it.isLastModifiedOlderThan2Weeks(date)) {
				try {
					it.delete()
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
		}
	}
	
	/* for old version below 1.2.6
		remove .opus all opus in main cache directory
	*/
	private fun removeAudioInCache(context: Context) {
		val date = Date(System.currentTimeMillis())
		context.cacheDir.listFiles()?.forEach {
			if (it.isFile && it.isLastModifiedOlderThan2Weeks(date)) {
				try {
					it.delete()
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
		}
	}
	
	private fun File.isLastModifiedOlderThan2Weeks(date: Date): Boolean {
		return (date.time - this.lastModified()) > TWO_WEEKS
	}
	
	companion object {
		const val TWO_WEEKS: Long = 60 * 1000 * 60 * 24 * 14
		private const val UNIQUE_WORK_KEY = "CleanupAudioCacheWorker"
		const val AUDIOS_SUB_DIRECTORY = "audios"
		
		fun enqueuePeriodically() {
			val workRequest = PeriodicWorkRequestBuilder<CleanupAudioCacheWorker>(1, TimeUnit.DAYS).build()
			WorkManager.getInstance().enqueueUniquePeriodicWork(UNIQUE_WORK_KEY, ExistingPeriodicWorkPolicy.REPLACE, workRequest)
		}
		
		fun workInfos(): LiveData<List<WorkInfo>> {
			return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
		}
	}
}