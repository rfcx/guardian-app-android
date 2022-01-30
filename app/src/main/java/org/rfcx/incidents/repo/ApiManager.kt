package org.rfcx.incidents.repo

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.entity.location.CheckIn
import org.rfcx.incidents.repo.serializer.CheckInSerializer
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
            .baseUrl(BuildConfig.CORE_API_BASE_URL)
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
                readTimeout(180, TimeUnit.SECONDS)
                writeTimeout(180, TimeUnit.SECONDS)
                if (BuildConfig.DEBUG) {
                    addNetworkInterceptor(StethoInterceptor())
                }
            }
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    private fun createDefaultGson(): Gson {
        val builder = GsonBuilder()
        builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        builder.registerTypeAdapter(CheckIn::class.java, CheckInSerializer())
        return builder.create()
    }
}
