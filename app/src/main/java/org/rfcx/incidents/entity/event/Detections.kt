package org.rfcx.incidents.entity.event

import java.util.Date

data class Detections(
    var streamId: String = "",
    var start: Date = Date(),
    var end: Date = Date(),
    var confidence: Double = 0.0,
    var classification: Classification = Classification(),
)
