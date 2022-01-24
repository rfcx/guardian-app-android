package org.rfcx.incidents.data.remote.service.rest

import io.reactivex.Single
import org.rfcx.incidents.entity.event.ClassificationBody
import org.rfcx.incidents.entity.event.ClassificationResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ClassifiedService {
	@POST("v1/tags/classified/byannotator")
	fun getClassificationSpectrogram(
			@Body body: ClassificationBody): Single<ClassificationResponse>
}
