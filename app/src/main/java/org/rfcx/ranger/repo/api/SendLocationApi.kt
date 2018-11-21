package org.rfcx.ranger.repo.api

import android.content.Context
import com.crashlytics.android.Crashlytics
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.entity.location.CheckIn
import org.rfcx.ranger.entity.location.CheckInResult
import org.rfcx.ranger.entity.location.RangerLocation
import org.rfcx.ranger.repo.*
import org.rfcx.ranger.util.getEmail
import org.rfcx.ranger.util.getTokenID
import org.rfcx.ranger.util.getUserGuId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SendLocationApi {
	
	fun checkIn(context: Context, locations: List<RangerLocation>, sendLocationCallBack: SendLocationCallBack) {
		
		val guid = context.getUserGuId()
		val token = context.getTokenID()
		val email = context.getEmail()
		if (guid == null || token == null || email == null) {
			sendLocationCallBack.onFailed(TokenExpireException(context), null)
			return
		}
		
		val authUser = "Bearer $token"
		val checkIn = CheckIn(locations)
		ApiManager.getInstance().apiRest.updateLocation(authUser, checkIn)
				.enqueue(object : Callback<List<CheckInResult>> {
					override fun onFailure(call: Call<List<CheckInResult>>?, t: Throwable?) {
						Crashlytics.logException(t)
						sendLocationCallBack.onFailed(t, t?.message)
					}
					
					override fun onResponse(call: Call<List<CheckInResult>>?, response: Response<List<CheckInResult>>?) {
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
	
	
	interface SendLocationCallBack : ApiCallback {
		fun onSuccess()
	}
}