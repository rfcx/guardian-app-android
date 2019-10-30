package org.rfcx.ranger.repo.api

import android.content.Context
import com.crashlytics.android.Crashlytics
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.entity.event.ReviewEventResponse
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.repo.*
import org.rfcx.ranger.util.getTokenID
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewEventApi {
	
	private val confirmEvent = "confirm"
	private var rejectEvent = "reject"
	
	fun reViewEvent(context: Context, event: Event, isReviewConfirm: Boolean, reviewEventCallback: ReviewEventCallback) {

		val token = context.getTokenID()
		if (token == null) {
			reviewEventCallback.onFailed(TokenExpireException(context), null)
			return
		}

		val authUser = "Bearer $token"
		
		ApiManager.getInstance().apiRest.reviewEvent(authUser, event.id, if (isReviewConfirm) confirmEvent else rejectEvent)
				.enqueue(object : Callback<ReviewEventResponse> {
					override fun onResponse(call: Call<ReviewEventResponse>?, response: Response<ReviewEventResponse>?) {
						val result = responseParser(response)
						when (result) {
							is Ok -> {
								reviewEventCallback.onSuccess()
							}
							is Err -> {
								responseErrorHandler(result.error, reviewEventCallback, context, "ReviewEventApi")
							}
						}
					}
					
					override fun onFailure(call: Call<ReviewEventResponse>?, t: Throwable?) {
						Crashlytics.logException(t)
						reviewEventCallback.onFailed(t, null)
					}
				})
	}
	
	interface ReviewEventCallback: ApiCallback {
		fun onSuccess()
	}
}