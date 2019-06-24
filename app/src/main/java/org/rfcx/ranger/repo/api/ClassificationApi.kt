package org.rfcx.ranger.repo.api

import android.content.Context
import com.google.gson.reflect.TypeToken
import org.rfcx.ranger.entity.Err
import org.rfcx.ranger.entity.Ok
import org.rfcx.ranger.entity.event.ClassificationBody
import org.rfcx.ranger.entity.event.ClassificationResponse
import org.rfcx.ranger.entity.event.Confidence
import org.rfcx.ranger.repo.*
import org.rfcx.ranger.util.GsonProvider
import org.rfcx.ranger.util.getTokenID
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClassificationApi {
	
	fun getClassification(context: Context?, audioGuids: String?, value: String?, annotatorGuid: String?, classificationCallback: ClassificationCallback) {
		
		if (context == null || audioGuids == null || value == null || annotatorGuid == null) return
		val token = context.getTokenID()
		if (token == null) {
			classificationCallback.onFailed(TokenExpireException(context), null)
			return
		}
		
		ApiManager.getInstance().apiRest.getClassificationSpectrogram("Bearer $token",
				ClassificationBody(audioGuids = audioGuids, value = value, annotatorGuid = annotatorGuid)).enqueue(object : Callback<ClassificationResponse> {
			override fun onFailure(call: Call<ClassificationResponse>, t: Throwable) {
			
			}
			
			override fun onResponse(call: Call<ClassificationResponse>, response: Response<ClassificationResponse>) {
				when (val result = responseParser(response)) {
					is Ok -> {
						result.value.let { it ->
							it.data?.attributes?.tags?.let { json ->
								try {
									val confidence = json.get(audioGuids)
									val confidences: List<Confidence> = GsonProvider.getInstance()
											.gson.fromJson(confidence, object : TypeToken<List<Confidence>>() {}.type)
									classificationCallback.onSuccess(confidences.filter {
										it.confidence == 1
									})
								} catch (e: Exception) {
									e.printStackTrace()
									classificationCallback.onFailed(e, e.message)
								}
							}
						}
					}
					is Err -> {
						responseErrorHandler(result.error, classificationCallback, context, "ClassificationApi")
					}
				}
			}
		})
	}
	
	interface ClassificationCallback : ApiCallback {
		fun onSuccess(confidences: List<Confidence>)
	}
}