package org.rfcx.incidents.data.api.incident

data class IncidentRequestFactory(
		val limit_incidents: Int = 1,
		val keyword: String = ""
)
