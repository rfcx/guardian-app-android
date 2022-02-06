package org.rfcx.incidents.data.interfaces

import io.reactivex.Single
import org.rfcx.incidents.entity.event.DetectionFactory
import org.rfcx.incidents.entity.event.Detections

interface DetectionsRepository {
    fun getDetections(factory: DetectionFactory): Single<List<Detections>>
}
