package org.rfcx.incidents.data.api.site

import org.rfcx.incidents.data.api.incident.IncidentResponse
import org.rfcx.incidents.entity.Stream

data class StreamResponse(
		var id: String = "",
		var name: String = "",
		var latitude: Double = 0.0,
		var longitude: Double = 0.0,
		var incidents: IncidentResponse = IncidentResponse(),
		var project: ProjectResponse = ProjectResponse()
)

data class ProjectResponse(
		var id: String = "",
		var name: String = ""
)

fun StreamResponse.toStream(): Stream = Stream(serverId = id, name = name, latitude = latitude, longitude = longitude, projectServerId = project.id, incidentRef = incidents.items[0].ref)
