package org.rfcx.incidents.entity.event

data class DetectionFactory(
    val id: String,
    val start: Long,
    val end: Long,
    val classifications: List<String>
)
