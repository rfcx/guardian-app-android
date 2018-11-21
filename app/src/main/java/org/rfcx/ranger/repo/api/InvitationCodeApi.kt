package org.rfcx.ranger.repo.api

import android.content.Context
import com.crashlytics.android.Crashlytics
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.entity.user.InvitationCodeResponse
import org.rfcx.ranger.repo.*
import org.rfcx.ranger.util.getTokenID
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Send an invitation code with a user token to assign the roles and membership required for the ranger app
 */

class InvitationCodeApi {

    fun send(context: Context, code: String, callback: InvitationCodeCallback) {
        val token = context.getTokenID()
        if (token == null) {
            callback.onFailed(TokenExpireException(context), null)
            return
        }

        ApiManager.getInstance().apiRest.sendInvitationCode("Bearer $token", code)
                .enqueue(object : Callback<InvitationCodeResponse> {
                    override fun onFailure(call: Call<InvitationCodeResponse>, t: Throwable) {
                        Crashlytics.logException(t)
                        callback.onFailed(t, null)
                    }
                    override fun onResponse(call: Call<InvitationCodeResponse>, response: Response<InvitationCodeResponse>) {
                        val result = responseParser(response)
                        when (result) {
                            is Ok -> {
                                if (result.value.success) {
                                    callback.onSuccess()
                                }
                                else {
                                    callback.onFailed(null, "Unsuccessful") // TODO: decide what to do
                                }
                            }
                            is Err -> {
                                responseErrorHandler(result.error, callback, context, "InvitationCodeApi")
                            }
                        }
                    }
                })
    }

    interface InvitationCodeCallback: ApiCallback {
        fun onSuccess()
    }
}