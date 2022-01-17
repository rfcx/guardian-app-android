package org.rfcx.incidents.data.api.site

data class StreamsRequestFactory(
		val limit: Int = 10,
		val offset: Int = 0,
		val limitIncidents: Int = 1,
		val projects: List<String>? = null
)
