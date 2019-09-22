package org.rfcx.ranger.service

import android.content.Context
import android.util.Log
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okio.BufferedSink
import okio.buffer
import okio.sink
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.data.local.EventDb
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit


class DownloadEventAudioWorker(val context: Context) {
	
	
	fun start() {
		val eventDb = EventDb()
		val events = eventDb.getEvents()
		val service = createRetrofit().create(AudioEndPoint::class.java)
		
		val file0 = events[0]
		
		val aa = service.getRawAudio(file0.audio!!.opus)
		aa.subscribeOn(Schedulers.io())
				.observeOn(Schedulers.io())
				.doOnNext {
					saveFile(it, file0.audioGUID!!)
							.doOnNext {
								Log.i("saveFile", it.absolutePath)
							}
							.doOnError {
							
							}
							.subscribe()
				}
				.subscribe()
	}
	
	private fun saveFile(response: Response<ResponseBody>, fileName: String): Observable<File> {
		return Observable.create { subscriber ->
			
			val temp = File(context.cacheDir, "$fileName _temp")
			val file = File(context.cacheDir, fileName)
			if (response.body()?.source() == null) {
				subscriber.onError(NullPointerException())
			}
			try {
				val sink: BufferedSink = temp.sink().buffer()
				sink.writeAll(response.body()!!.source())
				sink.close()
				temp.renameTo(file)
				subscriber.onNext(file)
				subscriber.onComplete()
				Log.d("saveFile", "SUccess")
			} catch (e: IOException) {
				e.printStackTrace()
				subscriber.onError(e)
			}
		}
	}
	
	private fun createRetrofit(): Retrofit {
		return Retrofit.Builder().baseUrl(BuildConfig.RANGER_DOMAIN)
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.client(OkHttpClient.Builder().apply {
					connectTimeout(30, TimeUnit.SECONDS)
					readTimeout(30, TimeUnit.SECONDS)
				}.build())
				.build()
	}
}

interface AudioEndPoint {
	@GET
	fun getRawAudio(@Url audioUrl: String): Observable<Response<ResponseBody>>
}
