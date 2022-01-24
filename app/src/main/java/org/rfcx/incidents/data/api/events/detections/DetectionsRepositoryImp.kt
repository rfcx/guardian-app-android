package org.rfcx.incidents.data.api.events.detections

import io.reactivex.Single
import org.rfcx.incidents.entity.alert.DetectionFactory
import org.rfcx.incidents.entity.alert.Detections

class DetectionsRepositoryImp(private val endpoint: DetectionsEndpoint) : DetectionsRepository {
	override fun getDetections(factory: DetectionFactory): Single<List<Detections>> {
		return endpoint.getDetections(factory.id, factory.start, factory.end, factory.classifications)
	}
}
