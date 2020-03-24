package org.rfcx.ranger.service

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import io.realm.Realm
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okio.BufferedSink
import okio.buffer
import okio.sink
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.service.CleanupAudioCacheWorker.Companion.TWO_WEEKS
import org.rfcx.ranger.util.RealmHelper
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class DownLoadEventWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
	private val audioDirectory = File(context.cacheDir, AUDIOS_SUB_DIRECTORY)
	private val needTobeDownloadEvent = arrayListOf<Event>()
	override fun doWork(): Result {
		Log.d(TAG, "doWork")
		
		if (!audioDirectory.exists()) {
			audioDirectory.mkdir()
		}
		val nowDate = Date(System.currentTimeMillis())
		val eventDb = EventDb(Realm.getInstance(RealmHelper.migrationConfig()))
		val events = eventDb.getEventsSync()
		needTobeDownloadEvent.clear()
		for (event in events) {
			if ((nowDate.time - event.beginsAt.time) <= TWO_WEEKS) {
				val file = File(audioDirectory, "${event.audioId}.opus")
				if (!file.exists())
					needTobeDownloadEvent.add(event)
			} else {
				break
			}
		}
		startDownload()
		return if (needTobeDownloadEvent.isNotEmpty()) Result.retry() else Result.success()
	}
	
	private fun startDownload() {
		if (needTobeDownloadEvent.isNotEmpty()) {
			downLoadFile(needTobeDownloadEvent[0]) { event, _ ->
				needTobeDownloadEvent.remove(event)
				startDownload()
			}
		}
	}
	
	private fun downLoadFile(event: Event, callback: (Event, Boolean) -> Unit) {
		val service = createRetrofit().create(AudioEndPoint::class.java)
		val aa: Call<ResponseBody>?
		aa = service.getRawAudio(event.audioOpusUrl)
		
		val body = aa.execute()
		if (body.isSuccessful) {
			saveFile(applicationContext, body, "${event.audioId}.opus") {
				callback.invoke(event, it)
			}
		} else {
			callback.invoke(event, false)
		}
	}
	
	private fun saveFile(context: Context, response: Response<ResponseBody>, fileName: String,
	                     callback: (Boolean) -> Unit) {
		val temp = File(audioDirectory, "$fileName _temp")
		val file = File(audioDirectory, fileName)
		
		if (file.exists()) {
			callback.invoke(true)
			return
		}
		
		if (response.body()?.source() == null) {
			callback.invoke(false)
			return
		}
		try {
			val sink: BufferedSink = temp.sink().buffer()
			sink.writeAll(response.body()!!.source())
			sink.close()
			temp.renameTo(file)
			callback.invoke(true)
			Log.d("saveFile", "$fileName Success")
		} catch (e: IOException) {
			e.printStackTrace()
			callback.invoke(false)
		}
	}
	
	private fun createRetrofit(): Retrofit {
		return Retrofit.Builder().baseUrl(BuildConfig.RANGER_DOMAIN)
				.client(OkHttpClient.Builder().apply {
					connectTimeout(30, TimeUnit.SECONDS)
					readTimeout(30, TimeUnit.SECONDS)
				}.build())
				.build()
	}
	
	companion object {
		private const val TAG = "DownLoadEventWorker"
		private const val UNIQUE_WORK_KEY = "DownLoadEventWorkerUniqueKey"
		const val AUDIOS_SUB_DIRECTORY = "audios"
		fun enqueue() {
			val constraints = Constraints.Builder()
					.setRequiredNetworkType(NetworkType.CONNECTED)
					.setRequiresStorageNotLow(true)
					.setRequiresDeviceIdle(false)
					.build()
			val workRequest = OneTimeWorkRequestBuilder<DownLoadEventWorker>().setConstraints(constraints).build()
			WorkManager.getInstance().enqueueUniqueWork(UNIQUE_WORK_KEY, ExistingWorkPolicy.REPLACE, workRequest)
		}
		
		fun workInfos(): LiveData<List<WorkInfo>> {
			return WorkManager.getInstance().getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_KEY)
		}
	}
}

interface AudioEndPoint {
	@GET
	fun getRawAudio(@Url audioUrl: String): Call<ResponseBody>
}