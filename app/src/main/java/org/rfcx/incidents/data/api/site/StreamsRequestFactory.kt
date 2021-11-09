package org.rfcx.incidents.data.api.site

data class StreamsRequestFactory(
		val limit: Int = 100,
		val offset: Int = 0,
		val withEventsCount: Boolean = true,
		val updatedAfter: String? = null,
		val sort: String? = "updated_at,name",
		val projects: List<String>? = null
)
