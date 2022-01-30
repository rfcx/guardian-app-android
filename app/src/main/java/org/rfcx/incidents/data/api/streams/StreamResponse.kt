package org.rfcx.incidents.data.api.streams

import org.rfcx.incidents.entity.Stream
import java.util.*

data class StreamResponse(
    var id: String = "",
    var name: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var incidents: IncidentListResponse = IncidentListResponse(),
    var project: ProjectResponse = ProjectResponse()
)

data class IncidentListResponse(
    var total: Int = 0,
    var items: List<IncidentResponse> = listOf()
)

data class IncidentResponse(
    var id: String = "",
    var ref: Int = 0,
    var events: List<EventResponse> = listOf()
)

data class EventResponse(
    var id: String = "",
    var start: Date,
    var end: Date,
    var classification: ClassificationResponse = ClassificationResponse()
)

data class ClassificationResponse(
    var value: String = "",
    var title: String = ""
)

data class ProjectResponse(
    var id: String = "",
    var name: String = ""
)

fun StreamResponse.toStream(): Stream = Stream(
    serverId = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    projectServerId = project.id,
    incidentRef = incidents.items[0].ref
)
