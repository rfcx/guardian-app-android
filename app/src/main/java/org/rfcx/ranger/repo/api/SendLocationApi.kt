package org.rfcx.ranger.repo.api

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.entity.Result
import org.rfcx.ranger.entity.location.CheckIn
import org.rfcx.ranger.entity.location.CheckInRequest
import org.rfcx.ranger.entity.location.CheckInResult
import org.rfcx.ranger.repo.*
import org.rfcx.ranger.util.getTokenID
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SendLocationApi {
	
	fun send(context: Context, locations: List<CheckIn>, callback: SendLocationCallback) {
		
		request(context, locations).enqueue(object : Callback<List<CheckInResult>> {
			override fun onFailure(call: Call<List<CheckInResult>>?, t: Throwable?) {
				FirebaseCrashlytics.getInstance().log(t?.message.toString())
				callback.onFailed(t, t?.message)
			}

			override fun onResponse(call: Call<List<CheckInResult>>?, response: Response<List<CheckInResult>>?) {
				val result = responseParser(response)
				when (result) {
					is Ok -> {
						callback.onSuccess()
					}
					is Err -> {
						responseErrorHandler(result.error, callback, context, "SendLocationApi")
					}
				}
			}
		})
	}

	fun sendSync(context: Context, locations: List<CheckIn>): Result<List<CheckInResult>, Exception> {

		val response: Response<List<CheckInResult>>?
		try {
			response = request(context, locations).execute()
		} catch (e: Exception) {
			return Err(e)
		}

		return responseParser(response)
	}

	private fun request(context: Context, locations: List<CheckIn>): Call<List<CheckInResult>> {
		val token = context.getTokenID()
		if (token == null) {
			throw Exception("Null token")
		}

		val authUser = "Bearer $token"
		val checkIn = CheckInRequest(locations)
		return ApiManager.getInstance().apiRest.updateLocation(authUser, checkIn)
	}
	
	
	interface SendLocationCallback : ApiCallback {
		fun onSuccess()
	}
}
