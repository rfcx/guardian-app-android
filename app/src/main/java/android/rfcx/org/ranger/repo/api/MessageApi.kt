package android.rfcx.org.ranger.repo.api

import android.content.Context
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.entity.ErrorResponse
import android.rfcx.org.ranger.entity.message.Message
import android.rfcx.org.ranger.repo.ApiManager
import android.rfcx.org.ranger.repo.TokenExpireException
import android.rfcx.org.ranger.util.GsonProvider
import android.rfcx.org.ranger.util.getEmail
import android.rfcx.org.ranger.util.getTokenID
import android.rfcx.org.ranger.util.getUserGuId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by Jingjoeh on 10/5/2017 AD.
 */
class MessageApi {
	
	fun getMessage(context: Context, onMessageCallBack: OnMessageCallBack) {
		
		val guid = context.getUserGuId()
		val token = context.getTokenID()
		val email = context.getEmail()
		if (guid == null || token == null || email == null) {
			onMessageCallBack.onFailed(TokenExpireException(), null)
			return
		}
		
		//val authUser = "user/$guid"
		val authUser = "Bearer $token"
		ApiManager.getInstance().apiRest.getMessage(authUser, email,
				"ranger-warning").enqueue(object : Callback<List<Message>> {
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
							onMessageCallBack.onFailed(TokenExpireException(), null)
							return
						}
						
						if (response.errorBody() != null) {
							try {
								val error: ErrorResponse = GsonProvider.getInstance().gson.fromJson(response.errorBody()?.string(), ErrorResponse::class.java)
								onMessageCallBack.onFailed(null, error.message)
							} catch (e: Exception) {
								onMessageCallBack.onFailed(null, context.getString(R.string.error_common))
							}
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

