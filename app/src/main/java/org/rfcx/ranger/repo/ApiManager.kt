package org.rfcx.ranger.repo

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.realm.RealmObject
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.entity.location.CheckIn
import org.rfcx.ranger.repo.serializer.CheckInSerializer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiManager {
	var apiRest: ApiRestInterface
	
	companion object {
		@Volatile
		private var INSTANCE: ApiManager? = null
		
		fun getInstance(): ApiManager =
				INSTANCE ?: synchronized(this) {
					INSTANCE ?: ApiManager()
				}
	}
	
	init {
		val retrofit = Retrofit.Builder()
				.addConverterFactory(GsonConverterFactory.create(createDefaultGson()))
				.baseUrl(BuildConfig.RANGER_DOMAIN)
				.client(createClient())
				.build()
		
		apiRest = retrofit.create(ApiRestInterface::class.java)
	}
	
	private fun createClient(): OkHttpClient {
		// okHttp log
		val httpLoggingInterceptor = HttpLoggingInterceptor()
		httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
		
		return OkHttpClient.Builder()
				.apply {
					readTimeout(30, TimeUnit.SECONDS)
					writeTimeout(30, TimeUnit.SECONDS)
					if (BuildConfig.DEBUG) {
						addNetworkInterceptor(StethoInterceptor())
					}
				}
				.addInterceptor(httpLoggingInterceptor)
				.build()
	}
	
	private fun createDefaultGson(): Gson {
		val builder = GsonBuilder()
		builder.registerTypeAdapter(CheckIn::class.java, CheckInSerializer())
		return builder.create()
	}
	
}