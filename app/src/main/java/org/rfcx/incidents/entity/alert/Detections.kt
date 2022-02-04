package org.rfcx.incidents.entity.alert

import org.rfcx.incidents.data.remote.events.ClassificationRequest
import java.util.*

data class Detections(
    var streamId: String = "",
    var start: Date = Date(),
    var end: Date = Date(),
    var confidence: Double = 0.0,
    var classification: ClassificationRequest = ClassificationRequest(),
)
