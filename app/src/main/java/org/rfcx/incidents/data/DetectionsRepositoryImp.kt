package org.rfcx.incidents.data

import io.reactivex.Single
import org.rfcx.incidents.data.interfaces.DetectionsRepository
import org.rfcx.incidents.data.remote.detections.DetectionsEndpoint
import org.rfcx.incidents.entity.event.DetectionFactory
import org.rfcx.incidents.entity.event.Detections

class DetectionsRepositoryImp(private val endpoint: DetectionsEndpoint) : DetectionsRepository {
    override fun getDetections(factory: DetectionFactory): Single<List<Detections>> {
        return endpoint.getDetections(factory.id, factory.start, factory.end, factory.classifications)
    }
}
