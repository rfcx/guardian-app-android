package android.rfcx.org.ranger.repo.api

import android.content.Context
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.entity.ErrorResponse
import android.rfcx.org.ranger.entity.LoginResponse
import android.rfcx.org.ranger.entity.Message
import android.rfcx.org.ranger.repo.ApiManager
import android.rfcx.org.ranger.repo.TokenExpireException
import android.rfcx.org.ranger.util.GsonProvider
import android.rfcx.org.ranger.util.PrefKey
import android.rfcx.org.ranger.util.PreferenceHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Jingjoeh on 10/5/2017 AD.
 */
class MessageApi {

    fun getMessage(context: Context, onMessageCallBack: OnMessageCallBack) {

        val loginRes: LoginResponse? = PreferenceHelper.getInstance(context)
                .getObject(PrefKey.LOGIN_RESPONSE, LoginResponse::class.java)

        if (loginRes == null) {
            onMessageCallBack.onFailed(TokenExpireException(), null)
            return
        }
        val authUser = "user/" + loginRes.guid
        ApiManager.getInstance().apiRest.getMessage(authUser, loginRes.tokens[0].token,
                loginRes.guid, "ranger-warning").enqueue(object : Callback<List<Message>> {
            override fun onFailure(call: Call<List<Message>>?, t: Throwable?) {
                onMessageCallBack.onFailed(t, null)
            }

            override fun onResponse(call: Call<List<Message>>?, response: Response<List<Message>>?) {
                response?.let {
                    if (it.isSuccessful) {

                        if (it.body() != null) {
                            onMessageCallBack.onSuccess(it.body()!!)
                        } else {
                            onMessageCallBack.onFailed(null, context.getString(R.string.error_common))
                        }

                    } else {

                        if (response.code() == 401) {
                            onMessageCallBack.onFailed(TokenExpireException(),null)
                            return
                        }

                        if (response.errorBody() != null) {
                            val error: ErrorResponse = GsonProvider.getInstance().gson.
                                    fromJson(response.errorBody()!!.string(), ErrorResponse::class.java)
                            onMessageCallBack.onFailed(null, error.message)
                        } else {
                            onMessageCallBack.onFailed(null, context.getString(R.string.error_common))
                        }
                    }

                }
            }

        })

    }

    interface OnMessageCallBack {
        fun onFailed(t: Throwable?, message: String?)
        fun onSuccess(messages: List<Message>)
    }
}

