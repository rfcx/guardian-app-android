package org.rfcx.incidents.data.api.project

data class ProjectsRequestFactory(
    val limit: Int = 100,
    val offset: Int = 0,
    val fields: List<String> = listOf("id", "name", "permissions")
)
