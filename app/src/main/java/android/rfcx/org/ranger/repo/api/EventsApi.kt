package android.rfcx.org.ranger.repo.api

import android.content.Context
import android.rfcx.org.ranger.BuildConfig
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.entity.ErrorResponse
import android.rfcx.org.ranger.entity.EventResponse
import android.rfcx.org.ranger.entity.LoginResponse
import android.rfcx.org.ranger.repo.ApiManager
import android.rfcx.org.ranger.repo.TokenExpireException
import android.rfcx.org.ranger.util.GsonProvider
import android.rfcx.org.ranger.util.PrefKey
import android.rfcx.org.ranger.util.PreferenceHelper
import android.rfcx.org.ranger.util.RemoteConfigKey
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class EventsApi {

    fun getEvents(context: Context, limit: Int, onEventsCallBack: OnEventsCallBack) {

        val loginRes: LoginResponse? = PreferenceHelper.getInstance(context)
                .getObject(PrefKey.LOGIN_RESPONSE, LoginResponse::class.java)

        if (loginRes == null) {
            onEventsCallBack.onFailed(TokenExpireException(), null)
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

        val siteID = (rangerRemote.getString(RemoteConfigKey.REMOTE_SITE_ID)).trim()

        val authUser = "user/" + loginRes.guid
        ApiManager.getInstance().apiRest.getEvents(authUser, loginRes.tokens[0].token, siteID, limit)
                .enqueue(object : Callback<EventResponse> {
                    override fun onFailure(call: Call<EventResponse>?, t: Throwable?) {
                        onEventsCallBack.onFailed(t, null)
                    }

                    override fun onResponse(call: Call<EventResponse>?, response: Response<EventResponse>?) {
                        response?.let {
                            if (it.isSuccessful) {

                                if (it.body() != null) {
                                    onEventsCallBack.onSuccess(it.body()!!)
                                } else {
                                    onEventsCallBack.onFailed(null, context.getString(R.string.error_common))
                                }

                            } else {

                                if (response.code() == 401) {
                                    onEventsCallBack.onFailed(TokenExpireException(), null)
                                    return
                                }

                                if (response.errorBody() != null) {
                                    try {
                                        val error: ErrorResponse = GsonProvider.getInstance().gson.
                                                fromJson(response.errorBody()?.string(), ErrorResponse::class.java)
                                        onEventsCallBack.onFailed(null, error.message)
                                    } catch (e: Exception) {
                                        onEventsCallBack.onFailed(null, context.getString(R.string.error_common))
                                    }
                                } else {
                                    onEventsCallBack.onFailed(null, context.getString(R.string.error_common))
                                }
                            }

                        }
                    }

                })

    }

    interface OnEventsCallBack {
        fun onFailed(t: Throwable?, message: String?)
        fun onSuccess(event: EventResponse)
    }
}