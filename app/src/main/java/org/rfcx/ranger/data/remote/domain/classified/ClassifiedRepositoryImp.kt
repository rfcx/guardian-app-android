package org.rfcx.ranger.data.remote.domain.classified

import com.google.gson.reflect.TypeToken
import io.reactivex.Single
import org.rfcx.ranger.data.remote.data.classified.ClassifiedRepository
import org.rfcx.ranger.data.remote.service.rest.ClassifiedService
import org.rfcx.ranger.entity.event.ClassificationBody
import org.rfcx.ranger.entity.event.Confidence
import org.rfcx.ranger.util.GsonProvider
import org.rfcx.ranger.view.alert.AlertBottomDialogViewModel.Companion.confidenceValue

class ClassifiedRepositoryImp(private val classifiedService: ClassifiedService) : ClassifiedRepository {
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