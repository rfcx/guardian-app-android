package org.rfcx.incidents.data.remote.service

import android.content.Context
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.data.api.assets.AssetsEndpoint
import org.rfcx.incidents.data.api.events.EventsEndpoint
import org.rfcx.incidents.data.api.incident.IncidentEndpoint
import org.rfcx.incidents.data.api.project.GetProjectsEndpoint
import org.rfcx.incidents.data.api.site.GetStreamsEndpoint
import org.rfcx.incidents.data.remote.guardianGroup.GuardianGroupEndpoint
import org.rfcx.incidents.data.remote.invitecode.InviteCodeEndpoint
import org.rfcx.incidents.data.remote.password.PasswordChangeEndpoint
import org.rfcx.incidents.data.remote.profilephoto.ProfilePhotoEndpoint
import org.rfcx.incidents.data.remote.response.CreateResponseEndpoint
import org.rfcx.incidents.data.remote.service.rest.ClassifiedService
import org.rfcx.incidents.data.remote.service.rest.EventService
import org.rfcx.incidents.data.remote.setusername.SetNameEndpoint
import org.rfcx.incidents.data.remote.shortlink.ShortLinkEndpoint
import org.rfcx.incidents.data.remote.site.SiteEndpoint
import org.rfcx.incidents.data.remote.subscribe.SubscribeEndpoint
import org.rfcx.incidents.data.remote.terms.TermsEndpoint
import org.rfcx.incidents.data.remote.usertouch.UserTouchEndPoint
import org.rfcx.incidents.util.GsonProvider
import org.rfcx.incidents.util.ImprovedDateTypeAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

object ServiceFactory {
	
	fun makeProjectsService(isDebug: Boolean, context: Context): GetProjectsEndpoint {
		return createRetrofit(BuildConfig.RANGER_API_DOMAIN, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
				GsonProvider.getInstance().gson)
				.create(GetProjectsEndpoint::class.java)
	}
	
	fun makeStreamsService(isDebug: Boolean, context: Context): GetStreamsEndpoint {
		return createRetrofit(BuildConfig.RANGER_API_DOMAIN, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
				GsonProvider.getInstance().gson)
				.create(GetStreamsEndpoint::class.java)
	}
	
	fun makeIncidentsService(isDebug: Boolean, context: Context): IncidentEndpoint {
		return createRetrofit(BuildConfig.RANGER_API_DOMAIN, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
				GsonProvider.getInstance().gson)
				.create(IncidentEndpoint::class.java)
	}
	
	fun makeEventsService(isDebug: Boolean, context: Context): EventsEndpoint {
		return createRetrofit(BuildConfig.RANGER_API_DOMAIN, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
				GsonProvider.getInstance().gson)
				.create(EventsEndpoint::class.java)
	}
	
	fun makeCreateResponseService(isDebug: Boolean, context: Context): CreateResponseEndpoint {
		return createRetrofit(BuildConfig.RANGER_API_DOMAIN, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
				GsonProvider.getInstance().gson)
				.create(CreateResponseEndpoint::class.java)
	}
	
	fun makeAssetsService(isDebug: Boolean, context: Context): AssetsEndpoint {
		return createRetrofit(BuildConfig.RANGER_API_DOMAIN, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
				GsonProvider.getInstance().gson)
				.create(AssetsEndpoint::class.java)
	}
	
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
	
	fun makePasswordService(isDebug: Boolean, context: Context): PasswordChangeEndpoint {
		return createRetrofit(BuildConfig.RANGER_DOMAIN, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
				GsonProvider.getInstance().gson)
				.create(PasswordChangeEndpoint::class.java)
	}
	
	fun makeProfilePhotoService(isDebug: Boolean, context: Context): ProfilePhotoEndpoint {
		return createRetrofit(BuildConfig.RANGER_DOMAIN, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
				GsonProvider.getInstance().gson)
				.create(ProfilePhotoEndpoint::class.java)
	}
	
	fun makeSubscribeService(isDebug: Boolean, context: Context): SubscribeEndpoint {
		return createRetrofit(BuildConfig.RANGER_DOMAIN, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
				GsonProvider.getInstance().gson)
				.create(SubscribeEndpoint::class.java)
	}
	
	fun makeTermsService(isDebug: Boolean, context: Context): TermsEndpoint {
		return createRetrofit(BuildConfig.RANGER_DOMAIN, createAuthTokenOkHttpClient(isDebug, AuthTokenInterceptor(context)),
				GsonProvider.getInstance().gson)
				.create(TermsEndpoint::class.java)
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
