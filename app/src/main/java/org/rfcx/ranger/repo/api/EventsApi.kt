package org.rfcx.ranger.repo.api

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.EventsResponse
import org.rfcx.ranger.util.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.repo.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventsApi {
	
	fun getEvents(context: Context, limit: Int, offset: Int, onEventsCallBack: OnEventsCallBack) {
		
		val token = context.getTokenID()
		if (token == null) {
			onEventsCallBack.onFailed(TokenExpireException(context), null)
			return
		}

		val rangerRemote = FirebaseRemoteConfig.getInstance()
		// config for debug
		val configSettings = FirebaseRemoteConfigSettings.Builder()
				.setDeveloperModeEnabled(BuildConfig.DEBUG)
				.build()
		
		rangerRemote.setConfigSettings(configSettings)
		rangerRemote.setDefaults(R.xml.ranger_remote_config_defualt)
		
		// cache config
		var cacheExpiration: Long = 3600 // 1 hour
		if (rangerRemote.info.configSettings.isDeveloperModeEnabled) {
			cacheExpiration = 0
		}
		rangerRemote.fetch(cacheExpiration).addOnCompleteListener {
			rangerRemote.activateFetched()
		}
		
		val group = context.getGuardianGroup()
		if (group == null) {
			onEventsCallBack.onFailed(null, context.getString(R.string.error_no_guardian_group_set))
			return
		}
		
		ApiManager.getInstance().apiRest.getEvents("Bearer $token", group, "begins_at", "DESC", limit, offset)
				.enqueue(object : Callback<EventsResponse> {
					override fun onFailure(call: Call<EventsResponse>?, t: Throwable?) {
						FirebaseCrashlytics.getInstance().log(t?.message.toString())
						onEventsCallBack.onFailed(t, null)
					}
					
					override fun onResponse(call: Call<EventsResponse>?, response: Response<EventsResponse>?) {
						val result = responseParser(response)
						when (result) {
							is Ok -> {
								onEventsCallBack.onSuccess(result.value)
							}
							is Err -> {
								responseErrorHandler(result.error, onEventsCallBack, context, "EventsApi")
							}
						}
					}
				})
		
	}
	
	interface OnEventsCallBack: ApiCallback {
		fun onSuccess(event: EventsResponse)
	}
}
