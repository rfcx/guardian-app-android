package org.rfcx.incidents.data.remote.domain.classified

import com.google.gson.reflect.TypeToken
import io.reactivex.Single
import org.rfcx.incidents.data.remote.data.classified.ClassifiedRepository
import org.rfcx.incidents.data.remote.service.rest.ClassifiedService
import org.rfcx.incidents.entity.event.ClassificationBody
import org.rfcx.incidents.entity.event.Confidence
import org.rfcx.incidents.util.GsonProvider

class ClassifiedRepositoryImp(private val classifiedService: ClassifiedService) : ClassifiedRepository {
	companion object {
		const val confidenceValue = 0.8
	}
	
	override fun getClassifiedCation(classificationBody: ClassificationBody): Single<List<Confidence>> {
		return classifiedService.getClassificationSpectrogram(classificationBody).map { it ->
			it.data?.attributes?.tags?.let { json ->
				try {
					val confidence = json.get(classificationBody.audioGuids)
					val confidences: List<Confidence> = GsonProvider.getInstance()
							.gson.fromJson(confidence, object : TypeToken<List<Confidence>>() {}.type)
					confidences.filter {
						it.confidence == confidenceValue
					}
				} catch (e: Exception) {
					e.printStackTrace()
					throw e
				}
			}
		}
	}
}
