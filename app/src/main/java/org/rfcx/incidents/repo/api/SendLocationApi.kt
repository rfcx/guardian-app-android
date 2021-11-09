package org.rfcx.incidents.repo.api

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.rfcx.incidents.entity.Err
import org.rfcx.incidents.entity.Ok
import org.rfcx.incidents.entity.Result
import org.rfcx.incidents.entity.location.CheckIn
import org.rfcx.incidents.entity.location.CheckInRequest
import org.rfcx.incidents.entity.location.CheckInResult
import org.rfcx.incidents.repo.*
import org.rfcx.incidents.util.getTokenID
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
