package org.rfcx.ranger.data.api.project

data class ProjectResponse(
		var id: String = "",
		var name: String = "",
		var permissions: List<String> = listOf()
)
