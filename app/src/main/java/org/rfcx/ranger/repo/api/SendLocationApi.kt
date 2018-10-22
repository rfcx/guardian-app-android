package org.rfcx.ranger.repo.api

import android.content.Context
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.CheckInResult
import org.rfcx.ranger.entity.ErrorResponse
import org.rfcx.ranger.repo.ApiManager
import org.rfcx.ranger.repo.TokenExpireException
import org.rfcx.ranger.util.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Jingjoeh on 10/8/2017 AD.
 */
class SendLocationApi {

    fun checkIn(context: Context, latitude: Double, longitude: Double, time: String, sendLocationCallBack: SendLocationCallBack) {
	    
	    val guid = context.getUserGuId()
	    val token = context.getTokenID()
	    val email = context.getEmail()
	    if (guid == null || token == null || email == null) {
		    sendLocationCallBack.onFailed(TokenExpireException(context), null)
		    return
	    }
	
	    //val authUser = "user/$guid"
	    val authUser = "Bearer $token"
	    
        ApiManager.getInstance().apiRest.updateLocation(authUser, latitude, longitude, time)
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