package android.rfcx.org.ranger.repo.api

import android.content.Context
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.entity.ErrorResponse
import android.rfcx.org.ranger.entity.LoginResponse
import android.rfcx.org.ranger.repo.ApiManager
import android.rfcx.org.ranger.util.GsonProvider
import android.rfcx.org.ranger.util.PrefKey
import android.rfcx.org.ranger.util.PreferenceHelper
import retrofit2.Call
import retrofit2.Response

/**
 * Created by Jingjoeh on 10/3/2017 AD.
 */
class LoginApi {

    fun login(context: Context, email: String, password: String, loginRemember: Int, loginCallback: OnLoginCallback) {

        ApiManager.getInstance().apiRest.login(email, password, loginRemember).enqueue(object : retrofit2.Callback<List<LoginResponse>> {

            override fun onFailure(call: Call<List<LoginResponse>>?, t: Throwable?) {
                loginCallback.onFailed(t, t?.message)
            }

            override fun onResponse(call: Call<List<LoginResponse>>?, response: Response<List<LoginResponse>>?) {
                response?.let {
                    if (it.isSuccessful) {

                        if (it.body() != null && it.body()!!.isNotEmpty()) {
                            val loginResponse = it.body()!![0]
                            // save response
                            PreferenceHelper.getInstance(context).putObject(PrefKey.LOGIN_RESPONSE, loginResponse)
                            loginCallback.onSuccess(loginResponse)
                        } else {
                            loginCallback.onFailed(null, context.getString(R.string.error_common))
                        }

                    } else {
                        if (response.errorBody() != null) {

                            try {
                                val error: ErrorResponse = GsonProvider.getInstance().gson.
                                        fromJson(response.errorBody()?.string(), ErrorResponse::class.java)
                                loginCallback.onFailed(null, error.message)
                            } catch (e: Exception) {
                                loginCallback.onFailed(null, context.getString(R.string.error_common))
                            }

                        } else {
                            loginCallback.onFailed(null, context.getString(R.string.error_common))
                        }
                    }

                }
            }

        }

        )
    }

    interface OnLoginCallback {
        fun onFailed(t: Throwable?, message: String?)
        fun onSuccess(loginResponse: LoginResponse?)
    }
}