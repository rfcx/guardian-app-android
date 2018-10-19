package android.rfcx.org.ranger.repo.api

import android.content.Context
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.entity.CheckInResult
import android.rfcx.org.ranger.entity.ErrorResponse
import android.rfcx.org.ranger.entity.LoginResponse
import android.rfcx.org.ranger.repo.ApiManager
import android.rfcx.org.ranger.repo.TokenExpireException
import android.rfcx.org.ranger.util.GsonProvider
import android.rfcx.org.ranger.util.PrefKey
import android.rfcx.org.ranger.util.PreferenceHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Jingjoeh on 10/8/2017 AD.
 */
class SendLocationApi {

    fun checkIn(context: Context, latitude: Double, longitude: Double, time: String, sendLocationCallBack: SendLocationCallBack) {


        val loginRes: LoginResponse? = PreferenceHelper.getInstance(context)
                .getObject(PrefKey.LOGIN_RESPONSE, LoginResponse::class.java)

        if (loginRes == null) {
            sendLocationCallBack.onFailed(TokenExpireException(context), null)
            return
        }

        val authUser = "user/" + loginRes.guid
        ApiManager.getInstance().apiRest.updateLocation(authUser, loginRes.tokens[0].token, latitude, longitude, time)
                .enqueue(object : Callback<CheckInResult> {
                    override fun onFailure(call: Call<CheckInResult>?, t: Throwable?) {
                        sendLocationCallBack.onFailed(t, t?.message)
                    }

                    override fun onResponse(call: Call<CheckInResult>?, response: Response<CheckInResult>?) {
                        response?.let {
                            if (it.isSuccessful) {

                                if (it.body() != null) {
                                    if (it.body() != null) {
                                        sendLocationCallBack.onSuccess()
                                    } else {
                                        sendLocationCallBack.onFailed(null, context.getString(R.string.error_common))
                                    }
                                }

                            } else {

                                if (response.code() == 401) {
                                    sendLocationCallBack.onFailed(TokenExpireException(context), null)
                                    return
                                }

                                if (response.errorBody() != null) {
                                    try {
                                        val error: ErrorResponse = GsonProvider.getInstance().gson.
                                                fromJson(response.errorBody()?.string(), ErrorResponse::class.java)
                                        sendLocationCallBack.onFailed(null, error.message)
                                    } catch (e: Exception) {
                                        sendLocationCallBack.onFailed(null, context.getString(R.string.error_common))
                                    }
                                } else {
                                    sendLocationCallBack.onFailed(null, context.getString(R.string.error_common))
                                }
                            }

                        }
                    }

                })
    }


    interface SendLocationCallBack {
        fun onSuccess()
        fun onFailed(t: Throwable?, message: String?)
    }
}