package android.rfcx.org.ranger.repo

import android.rfcx.org.ranger.BuildConfig
import android.rfcx.org.ranger.repo.retofit.ApiRestInterface
import android.rfcx.org.ranger.util.GsonProvider
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient
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
        return OkHttpClient.Builder()
                .apply {
                    readTimeout(30, TimeUnit.SECONDS)
                    writeTimeout(30, TimeUnit.SECONDS)
                    if (BuildConfig.DEBUG) {
                        addNetworkInterceptor(StethoInterceptor())
                    }
                }
                .build()
    }

}