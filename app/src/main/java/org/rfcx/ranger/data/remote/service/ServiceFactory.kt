package org.rfcx.ranger.data.remote.service

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.data.remote.groupByGuardians.GroupByGuardiansEndpoint
import org.rfcx.ranger.data.remote.guardianGroup.GuardianGroupEndpoint
import org.rfcx.ranger.data.remote.invitecode.InviteCodeEndpoint
import org.rfcx.ranger.data.remote.service.rest.ClassifiedService
import org.rfcx.ranger.data.remote.service.rest.EventService
import org.rfcx.ranger.data.remote.setusername.SetNameEndpoint
import org.rfcx.ranger.data.remote.shortlink.ShortLinkEndpoint
import org.rfcx.ranger.data.remote.site.SiteEndpoint
import org.rfcx.ranger.data.remote.usertouch.UserTouchEndPoint
import org.rfcx.ranger.util.GsonProvider
import org.rfcx.ranger.util.ImprovedDateTypeAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

object ServiceFactory {
	
	fun makeEventService(isDebug: Boolean, context: Context): EventService {
		return createRetrofit(BuildConfig.RANGER_DOMAIN, createAuthTokenOkHttpClient(isDebug,
				AuthTokenInterceptor(context)), createDateGson())
				.create(EventService::class.java)
	}
	
	fun makeClassifiedService(isDebug: Boolean, context: Context): ClassifiedService {
		return createRetrofit(BuildConfig.RANGER_DOMAIN, createAuthTokenOkHttpClient(isDebug,
				AuthTokenInterceptor(context)), GsonProvider.getInstance().gson)
				.create(ClassifiedService::class.java)
	}
	
	fun makeGuardianGroupService(isDebug: Boolean, context: Context): GuardianGroupEndpoint {
		return createRetrofit(BuildConfig.RANGER_DOMAIN, createAuthTokenOkHttpClient(isDebug,
				AuthTokenInterceptor(context)), GsonProvider.getInstance().gson)
				.create(GuardianGroupEndpoint::class.java)
	}
	
	fun makeInviteCodeService(isDebug: Boolean, context: Context): InviteCodeEndpoint {
		return createRetrofit(BuildConfig.RANGER_DOMAIN, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
				GsonProvider.getInstance().gson)
				.create(InviteCodeEndpoint::class.java)
	}
	
	fun makeUserTouchService(isDebug: Boolean, context: Context): UserTouchEndPoint {
		return createRetrofit(BuildConfig.RANGER_DOMAIN, createAuthTokenOkHttpClient(isDebug,
				AuthTokenInterceptor(context)), GsonProvider.getInstance().gson)
				.create(UserTouchEndPoint::class.java)
	}
	
	fun makeSetNameService(isDebug: Boolean, context: Context): SetNameEndpoint {
		return createRetrofit(BuildConfig.RANGER_DOMAIN, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
				GsonProvider.getInstance().gson)
				.create(SetNameEndpoint::class.java)
	}
	
	fun makeGroupByGuardiansService(isDebug: Boolean, context: Context): GroupByGuardiansEndpoint {
		return createRetrofit(BuildConfig.RANGER_DOMAIN, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
				GsonProvider.getInstance().gson)
				.create(GroupByGuardiansEndpoint::class.java)
	}
	
	fun makeSiteNameService(isDebug: Boolean, context: Context): SiteEndpoint {
		return createRetrofit(BuildConfig.RANGER_DOMAIN, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
				GsonProvider.getInstance().gson)
				.create(SiteEndpoint::class.java)
	}
	
	fun makeShortLinkService(isDebug: Boolean, context: Context): ShortLinkEndpoint {
		return createRetrofit(BuildConfig.RANGER_DOMAIN, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
				GsonProvider.getInstance().gson)
				.create(ShortLinkEndpoint::class.java)
	}
	
	private fun createRetrofit(baseUrl: String, okHttpClient: OkHttpClient, gson: Gson): Retrofit {
		return Retrofit.Builder().baseUrl(baseUrl)
				.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
				.addConverterFactory(GsonConverterFactory.create(gson))
				.client(okHttpClient)
				.build()
	}
	
	private fun createDateGson(): Gson {
		val builder = GsonBuilder()
		builder.setLenient()
		builder.registerTypeAdapter(Date::class.java, ImprovedDateTypeAdapter())
		
		return builder.create()
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