package org.rfcx.ranger.data.remote.data.classified

import io.reactivex.Single
import org.rfcx.ranger.entity.event.ClassificationBody
import org.rfcx.ranger.entity.event.Confidence

interface ClassifiedRepository {
	fun getClassifiedCation(classificationBody: ClassificationBody): Single<List<Confidence>>
}