package org.rfcx.incidents.data.remote.data.classified

import io.reactivex.Single
import org.rfcx.incidents.entity.event.ClassificationBody
import org.rfcx.incidents.entity.event.Confidence

interface ClassifiedRepository {
	fun getClassifiedCation(classificationBody: ClassificationBody): Single<List<Confidence>>
}
