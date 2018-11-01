package org.rfcx.ranger.repo.api

import android.content.Context
import com.crashlytics.android.Crashlytics
import org.rfcx.ranger.entity.CheckInResult
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.repo.*
import org.rfcx.ranger.util.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SendLocationApi {

    fun checkIn(context: Context, latitude: Double, longitude: Double, time: String, sendLocationCallBack: SendLocationCallBack) {
	    
	    val guid = context.getUserGuId()
	    val token = context.getTokenID()
	    val email = context.getEmail()
	    if (guid == null || token == null || email == null) {
		    sendLocationCallBack.onFailed(TokenExpireException(context), null)
		    return
	    }

	    val authUser = "Bearer $token"
	    
        ApiManager.getInstance().apiRest.updateLocation(authUser, latitude, longitude, time)
                .enqueue(object : Callback<CheckInResult> {
                    override fun onFailure(call: Call<CheckInResult>?, t: Throwable?) {
                        Crashlytics.logException(t)
                        sendLocationCallBack.onFailed(t, t?.message)
                    }

                    override fun onResponse(call: Call<CheckInResult>?, response: Response<CheckInResult>?) {
                        val result = responseParser(response)
                        when (result) {
                            is Ok -> {
                                sendLocationCallBack.onSuccess()
                            }
                            is Err -> {
                                responseErrorHandler(result.error, sendLocationCallBack, context, "SendLocationApi")
                            }
                        }
                    }

                })
    }


    interface SendLocationCallBack: ApiCallback {
        fun onSuccess()
    }
}