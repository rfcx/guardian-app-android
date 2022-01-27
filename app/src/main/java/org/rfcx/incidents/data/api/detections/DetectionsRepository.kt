package org.rfcx.incidents.data.api.detections

import io.reactivex.Single
import org.rfcx.incidents.entity.alert.DetectionFactory
import org.rfcx.incidents.entity.alert.Detections

interface DetectionsRepository {
    fun getDetections(factory: DetectionFactory): Single<List<Detections>>
}
