package org.rfcx.ranger.repo.api

import android.content.Context
import com.crashlytics.android.Crashlytics
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.entity.message.Message
import org.rfcx.ranger.repo.*
import org.rfcx.ranger.util.getEmail
import org.rfcx.ranger.util.getTokenID
import org.rfcx.ranger.util.getUserGuId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessageApi {
	
	fun getMessage(context: Context, onMessageCallBack: OnMessageCallBack) {
		
		val guid = context.getUserGuId()
		val token = context.getTokenID()
		val email = context.getEmail()
		if (guid == null || token == null || email == null) {
			onMessageCallBack.onFailed(TokenExpireException(context), null)
			return
		}

		val authUser = "Bearer $token"
		ApiManager.getInstance().apiRest.getMessage(authUser, email,
				"ranger-warning").enqueue(object : Callback<List<Message>> {
			override fun onFailure(call: Call<List<Message>>?, t: Throwable?) {
				Crashlytics.logException(t)
				onMessageCallBack.onFailed(t, null)
			}
			
			override fun onResponse(call: Call<List<Message>>?, response: Response<List<Message>>?) {
				val result = responseParser(response)
				when (result) {
					is Ok -> {
						onMessageCallBack.onSuccess(result.value)
					}
					is Err -> {
						responseErrorHandler(result.error, onMessageCallBack, context, "MessageApi")
					}
				}
			}
			
		})
		
	}
	
	interface OnMessageCallBack: ApiCallback {
		fun onSuccess(messages: List<Message>)
	}
}

