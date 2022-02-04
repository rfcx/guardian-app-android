package org.rfcx.incidents.data.remote.common.service

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.data.remote.assets.AssetsEndpoint
import org.rfcx.incidents.data.remote.detections.DetectionsEndpoint
import org.rfcx.incidents.data.remote.events.EventsEndpoint
import org.rfcx.incidents.data.remote.media.MediaEndpoint
import org.rfcx.incidents.data.remote.password.PasswordChangeEndpoint
import org.rfcx.incidents.data.remote.profilephoto.ProfilePhotoEndpoint
import org.rfcx.incidents.data.remote.project.ProjectsEndpoint
import org.rfcx.incidents.data.remote.response.CreateResponseEndpoint
import org.rfcx.incidents.data.remote.setusername.SetNameEndpoint
import org.rfcx.incidents.data.remote.streams.GetStreamsEndpoint
import org.rfcx.incidents.data.remote.subscribe.SubscribeEndpoint
import org.rfcx.incidents.data.remote.usertouch.UserTouchEndPoint
import org.rfcx.incidents.util.GsonProvider
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ServiceFactory {

    fun makeProjectsService(isDebug: Boolean, context: Context): ProjectsEndpoint {
        return createRetrofit(
            BuildConfig.RANGER_API_BASE_URL, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
            GsonProvider.getInstance().gson
        )
            .create(ProjectsEndpoint::class.java)
    }

    fun makeStreamsService(isDebug: Boolean, context: Context): GetStreamsEndpoint {
        return createRetrofit(
            BuildConfig.RANGER_API_BASE_URL, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
            GsonProvider.getInstance().gson
        )
            .create(GetStreamsEndpoint::class.java)
    }

    fun makeDetectionsService(isDebug: Boolean, context: Context): DetectionsEndpoint {
        return createRetrofit(
            BuildConfig.RANGER_API_BASE_URL, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
            GsonProvider.getInstance().gson
        )
            .create(DetectionsEndpoint::class.java)
    }

    fun makeMediaService(isDebug: Boolean, context: Context): MediaEndpoint {
        return createRetrofit(
            BuildConfig.RANGER_API_BASE_URL, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
            GsonProvider.getInstance().gson
        )
            .create(MediaEndpoint::class.java)
    }

    fun makeEventsService(isDebug: Boolean, context: Context): EventsEndpoint {
        return createRetrofit(
            BuildConfig.RANGER_API_BASE_URL, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
            GsonProvider.getInstance().gson
        )
            .create(EventsEndpoint::class.java)
    }

    fun makeCreateResponseService(isDebug: Boolean, context: Context): CreateResponseEndpoint {
        return createRetrofit(
            BuildConfig.RANGER_API_BASE_URL, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
            GsonProvider.getInstance().gson
        )
            .create(CreateResponseEndpoint::class.java)
    }

    fun makeAssetsService(isDebug: Boolean, context: Context): AssetsEndpoint {
        return createRetrofit(
            BuildConfig.RANGER_API_BASE_URL, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
            GsonProvider.getInstance().gson
        )
            .create(AssetsEndpoint::class.java)
    }

    fun makeUserTouchService(isDebug: Boolean, context: Context): UserTouchEndPoint {
        return createRetrofit(
            BuildConfig.CORE_API_BASE_URL,
            createAuthTokenOkHttpClient(
                isDebug,
                AuthTokenInterceptor(context)
            ),
            GsonProvider.getInstance().gson
        )
            .create(UserTouchEndPoint::class.java)
    }

    fun makeSetNameService(isDebug: Boolean, context: Context): SetNameEndpoint {
        return createRetrofit(
            BuildConfig.CORE_API_BASE_URL, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
            GsonProvider.getInstance().gson
        )
            .create(SetNameEndpoint::class.java)
    }

    fun makePasswordService(isDebug: Boolean, context: Context): PasswordChangeEndpoint {
        return createRetrofit(
            BuildConfig.CORE_API_BASE_URL, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
            GsonProvider.getInstance().gson
        )
            .create(PasswordChangeEndpoint::class.java)
    }

    fun makeProfilePhotoService(isDebug: Boolean, context: Context): ProfilePhotoEndpoint {
        return createRetrofit(
            BuildConfig.CORE_API_BASE_URL, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
            GsonProvider.getInstance().gson
        )
            .create(ProfilePhotoEndpoint::class.java)
    }

    fun makeSubscribeService(isDebug: Boolean, context: Context): SubscribeEndpoint {
        return createRetrofit(
            BuildConfig.CORE_API_BASE_URL, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
            GsonProvider.getInstance().gson
        )
            .create(SubscribeEndpoint::class.java)
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
