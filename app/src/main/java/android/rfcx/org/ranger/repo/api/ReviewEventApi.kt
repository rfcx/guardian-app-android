package android.rfcx.org.ranger.repo.api

import android.content.Context
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.entity.ErrorResponse
import android.rfcx.org.ranger.entity.ReviewEventResponse
import android.rfcx.org.ranger.entity.event.Event
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
 * Created by Jingjoeh on 11/6/2017 AD.
 */
class ReviewEventApi {
	
	private val confirmEvent = "confirm"
	private var rejectEvent = "reject"
	
	fun reViewEvent(context: Context, event: Event, isReviewConfirm: Boolean, reviewEventCallback: ReviewEventCallback) {
		
		val guid = context.getUserGuId()
		val token = context.getTokenID()
		val email = context.getEmail()
		if (guid == null || token == null || email == null) {
			reviewEventCallback.onFailed(TokenExpireException(context), null)
			return
		}
		
		//val authUser = "user/$guid"
		val authUser = "Bearer $token"
		
		ApiManager.getInstance().apiRest.reviewEvent(authUser, event.event_guid, if (isReviewConfirm) confirmEvent else rejectEvent)
				.enqueue(object : Callback<ReviewEventResponse> {
					override fun onResponse(call: Call<ReviewEventResponse>?, response: Response<ReviewEventResponse>?) {
						response?.let {
							if (it.isSuccessful) {
								
								if (it.body() != null) {
									if (it.body() != null) {
										reviewEventCallback.onSuccess()
									} else {
										reviewEventCallback.onFailed(null, context.getString(R.string.error_common))
									}
								}
								
							} else {
								
								if (response.code() == 401) {
									reviewEventCallback.onFailed(TokenExpireException(context), null)
									return
								}
								
								if (response.errorBody() != null) {
									try {
										val error: ErrorResponse = GsonProvider.getInstance().gson.fromJson(response.errorBody()?.string(), ErrorResponse::class.java)
										reviewEventCallback.onFailed(null, error.message)
									} catch (e: Exception) {
										reviewEventCallback.onFailed(null, context.getString(R.string.error_common))
									}
								} else {
									reviewEventCallback.onFailed(null, context.getString(R.string.error_common))
								}
							}
							
						}
						
					}
					
					override fun onFailure(call: Call<ReviewEventResponse>?, t: Throwable?) {
						reviewEventCallback.onFailed(t, null)
					}
					
				})
	}
	
	interface ReviewEventCallback {
		fun onSuccess()
		fun onFailed(t: Throwable?, message: String?)
	}
}