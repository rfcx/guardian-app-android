package org.rfcx.ranger.service

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okio.BufferedSink
import okio.buffer
import okio.sink
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.entity.event.Event
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Url
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class DownLoadEvent(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
	
	override fun doWork(): Result {
		Log.d(TAG, "doWork")
		val eventDb = EventDb()
		val events = eventDb.getEventsSync()
		val needTobeDownloadEvent = arrayListOf<Event>()
		
		
		for (event in events) {
			
			val file = File(applicationContext.cacheDir, "${event.audioGUID}.opus")
			if (!file.exists()) {
				needTobeDownloadEvent.add(event)
			}
			if (needTobeDownloadEvent.size == 1) {
				break
			}
		}
		if (needTobeDownloadEvent.isNotEmpty()) {
			Log.w("saveFile", "${needTobeDownloadEvent.count()}")
			val downloadEvent = needTobeDownloadEvent[0]
			val service = createRetrofit().create(AudioEndPoint::class.java)
			downloadEvent.audio?.opus?.let {
				val aa = service.getRawAudio(it)
				val body = aa.execute()
				if (body.isSuccessful) {
					saveFile(applicationContext, body, "${downloadEvent.audioGUID}.opus")
				}
			}
		}
		return if (needTobeDownloadEvent.isNotEmpty()) Result.retry() else Result.success()
	}
	
	private fun saveFile(context: Context, response: Response<ResponseBody>, fileName: String) {
		val temp = File(context.cacheDir, "$fileName _temp")
		val file = File(context.cacheDir, fileName)
		
		if (file.exists()) {
			return
		}
		
		if (response.body()?.source() == null) {
			return
		}
		try {
			val sink: BufferedSink = temp.sink().buffer()
			sink.writeAll(response.body()!!.source())
			sink.close()
			temp.renameTo(file)
			Log.d("saveFile", "$fileName Success")
		} catch (e: IOException) {
			e.printStackTrace()
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
		
		fun enqueue() {
			val constraints = Constraints.Builder()
					.setRequiredNetworkType(NetworkType.CONNECTED)
					.setRequiresStorageNotLow(true)
					.setRequiresDeviceIdle(false)
					.build()
			val workRequest = OneTimeWorkRequestBuilder<DownLoadEvent>().setConstraints(constraints).build()
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