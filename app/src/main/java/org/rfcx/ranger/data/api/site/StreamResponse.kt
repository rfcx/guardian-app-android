package org.rfcx.ranger.data.api.site

import org.rfcx.ranger.entity.ProjectInStream
import org.rfcx.ranger.entity.Stream

data class StreamResponse(
		var id: String = "",
		var name: String = "",
		var latitude: Double = 0.0,
		var longitude: Double = 0.0,
		var eventsCount: Int = 0,
		var project: ProjectResponse? = null
)

data class ProjectResponse(
		var id: String? = null,
		var name: String = ""
)

fun StreamResponse.toStream(): Stream = Stream(serverId = id, name = name, latitude = latitude, longitude = longitude, project = project?.toProjectInStream())

fun ProjectResponse.toProjectInStream(): ProjectInStream = ProjectInStream(id = id, name = name)