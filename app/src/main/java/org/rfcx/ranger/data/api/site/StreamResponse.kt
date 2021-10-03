package org.rfcx.ranger.data.api.site

data class StreamResponse(
		var id: String = "",
		var name: String = "",
		var latitude: Double = 0.0,
		var longitude: Double = 0.0,
		var project: ProjectResponse? = null
)

data class ProjectResponse(
		var id: String? = null,
		var name: String = ""
)
