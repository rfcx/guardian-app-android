package org.rfcx.ranger.data.remote.service

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.data.remote.service.rest.EventService
import org.rfcx.ranger.util.GsonProvider
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ServiceFatory {
	
	fun makeEventService(isDebug: Boolean, context: Context): EventService {
		return createRetrofit(BuildConfig.RANGER_DOMAIN, createAuthTokenOkHttpClient(isDebug,
				AuthTokenInterceptor(context)), GsonProvider.getInstance().gson)
				.create(EventService::class.java)
	}
	
	private fun createRetrofit(baseUrl: String, okHttpClient: OkHttpClient, gson: Gson): Retrofit {
		return Retrofit.Builder().baseUrl(baseUrl)
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.addConverterFactory(GsonConverterFactory.create(gson))
				.client(okHttpClient)
				.build()
	}
	
	/**
	 * OkHttp Client for auto add Auth header
	 */
	private fun createAuthTokenOkHttpClient(
			isDebug: Boolean,
			tokenInterceptor: AuthTokenInterceptor
	): OkHttpClient {
		val okHttpClient = OkHttpClient.Builder()
				.addInterceptor(tokenInterceptor)
				.connectTimeout(30, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS)
		
		if (isDebug) {
			okHttpClient
					.addNetworkInterceptor(StethoInterceptor())
					.addNetworkInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
		}
		return okHttpClient.build()
	}
	
	private fun createDefaultOkHttpClient(
			isDebug: Boolean
	): OkHttpClient {
		val okHttpClient = OkHttpClient.Builder()
				.connectTimeout(30, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS)
		
		if (isDebug) {
			okHttpClient
					.addNetworkInterceptor(StethoInterceptor())
					.addNetworkInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
		}
		return okHttpClient.build()
	}
}