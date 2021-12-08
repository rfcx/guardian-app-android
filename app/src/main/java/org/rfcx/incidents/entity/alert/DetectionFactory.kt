package org.rfcx.incidents.entity.alert

data class DetectionFactory(
		val id: String,
		val start: Long,
		val end: Long,
		val classifications: List<String>
)
