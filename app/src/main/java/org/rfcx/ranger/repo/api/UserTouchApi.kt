package org.rfcx.ranger.repo.api

import android.content.Context
import com.crashlytics.android.Crashlytics
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.entity.user.UserTouchResponse
import org.rfcx.ranger.repo.*
import org.rfcx.ranger.util.getTokenID
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserTouchApi {

    fun send(context: Context, callback: UserTouchCallback) {

        val token = context.getTokenID()
        if (token == null) {
            callback.onFailed(TokenExpireException(context), null)
            return
        }

        ApiManager.getInstance().apiRest.userTouch("Bearer $token")
                .enqueue(object : Callback<UserTouchResponse> {
                    override fun onResponse(call: Call<UserTouchResponse>?, response: Response<UserTouchResponse>?) {
                        val result = responseParser(response)
                        when (result) {
                            is Ok -> {
                                callback.onSuccess()
                            }
                            is Err -> {
                                responseErrorHandler(result.error, callback, context, "ReviewEventApi")
                            }
                        }
                    }

                    override fun onFailure(call: Call<UserTouchResponse>?, t: Throwable?) {
                        Crashlytics.logException(t)
                        callback.onFailed(t, null)
                    }

                })
    }

    interface UserTouchCallback: ApiCallback {
        fun onSuccess()
    }
}
