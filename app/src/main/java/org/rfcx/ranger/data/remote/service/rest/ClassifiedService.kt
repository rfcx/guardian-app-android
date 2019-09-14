package org.rfcx.ranger.data.remote.service.rest

import io.reactivex.Single
import org.rfcx.ranger.entity.event.ClassificationBody
import org.rfcx.ranger.entity.event.ClassificationResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ClassifiedService {
	@POST("tags/classified/byannotator")
	fun getClassificationSpectrogram(
			@Body body: ClassificationBody): Single<ClassificationResponse>
}