package org.rfcx.incidents.data.api.incident

import org.rfcx.incidents.data.api.site.ProjectResponse

data class IncidentsResponse(
		var id: String = "",
		var name: String = "",
		var latitude: Double = 0.0,
		var longitude: Double = 0.0,
		var project: ProjectResponse = ProjectResponse(),
		var incidents: IncidentResponse = IncidentResponse()
)

data class IncidentResponse(
		var items: List<IncidentItemsResponse> = listOf()
)

data class IncidentItemsResponse(
		var id: String = "",
		var ref: Int = 0,
		var streamId: String = "",
		var projectId: String = "",
)
