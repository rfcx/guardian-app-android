package org.rfcx.ranger.repo

import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.repo.retofit.ApiRestInterface
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by Jingjoeh on 10/2/2017 AD.
 */
class ApiManager {
    var apiRest: ApiRestInterface

    companion object {
        @Volatile private var INSTANCE: ApiManager? = null
        fun getInstance(): ApiManager =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: ApiManager()
                }
    }

    init {
        val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
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

}