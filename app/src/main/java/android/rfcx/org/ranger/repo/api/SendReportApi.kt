package android.rfcx.org.ranger.repo.api

import android.content.Context
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.entity.CheckInResult
import android.rfcx.org.ranger.entity.ErrorResponse
import android.rfcx.org.ranger.entity.LoginResponse
import android.rfcx.org.ranger.entity.report.Report
import android.rfcx.org.ranger.entity.report.SendReportResponse
import android.rfcx.org.ranger.repo.ApiManager
import android.rfcx.org.ranger.repo.TokenExpireException
import android.rfcx.org.ranger.util.GsonProvider
import android.rfcx.org.ranger.util.PrefKey
import android.rfcx.org.ranger.util.PreferenceHelper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Jingjoeh on 10/22/2017 AD.
 */

class SendReportApi {

    fun sendReport(context: Context, report: Report, sendReportCallback: SendReportCallback) {

        val loginRes: LoginResponse? = PreferenceHelper.getInstance(context)
                .getObject(PrefKey.LOGIN_RESPONSE, LoginResponse::class.java)

        if (loginRes == null) {
            sendReportCallback.onFailed(TokenExpireException(), null)
            return
        }

        val authUser = "user/" + loginRes.guid
        ApiManager.getInstance().apiRest.sendReport(authUser, loginRes.tokens[0].token, report)
                .enqueue(object : Callback<SendReportResponse> {
                    override fun onFailure(call: Call<SendReportResponse>?, t: Throwable?) {
                        sendReportCallback.onFailed(t, t?.message)
                    }

                    override fun onResponse(call: Call<SendReportResponse>?, response: Response<SendReportResponse>?) {
                        response?.let {
                            if (it.isSuccessful) {

                                if (it.body() != null) {
                                    if (it.body() != null) {
                                        sendReportCallback.onSuccess()
                                    } else {
                                        sendReportCallback.onFailed(null, context.getString(R.string.error_common))
                                    }
                                }

                            } else {

                                if (response.code() == 401) {
                                    sendReportCallback.onFailed(TokenExpireException(), null)
                                    return
                                }

                                if (response.errorBody() != null) {
                                    try {
                                        val error: ErrorResponse = GsonProvider.getInstance().gson.
                                                fromJson(response.errorBody()?.string(), ErrorResponse::class.java)
                                        sendReportCallback.onFailed(null, error.message)
                                    } catch (e: Exception) {
                                        sendReportCallback.onFailed(null, context.getString(R.string.error_common))
                                    }
                                } else {
                                    sendReportCallback.onFailed(null, context.getString(R.string.error_common))
                                }
                            }

                        }
                    }

                })

    }


    interface SendReportCallback {
        fun onSuccess()
        fun onFailed(t: Throwable?, message: String?)
    }
}