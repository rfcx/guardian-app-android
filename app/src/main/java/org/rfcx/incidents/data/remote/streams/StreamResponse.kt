package org.rfcx.incidents.data.remote.streams

import org.rfcx.incidents.entity.Classification
import org.rfcx.incidents.entity.Incident
import org.rfcx.incidents.entity.Stream
import org.rfcx.incidents.entity.alert.Alert
import org.rfcx.incidents.entity.event.Event
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
    var closeAt: Date,
    var createdAt: Date,
    var events: List<EventResponse> = listOf()
)

data class EventResponse(
    var id: String = "",
    var start: Date,
    var end: Date,
    var createdAt: Date,
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

fun IncidentResponse.toIncident(): Incident = Incident(
    id = this.id,
    closedAt = this.closeAt,
    createdAt = this.createdAt
)

fun ClassificationResponse.toClassification(): Classification = Classification(
    value = this.value,
    title = this.title
)


fun StreamResponse.toAlerts(): List<Alert> {
    val alerts = arrayListOf<Alert>()

    this.incidents.items.forEach { incident ->
        incident.events.forEach { event ->
            alerts.add(
                Alert(
                    serverId = event.id,
                    start = event.start,
                    end = event.end,
                    name = "????",
                    streamId = this.id,
                    projectId = this.project.id,
                    createdAt = event.createdAt,
                    classification = event.classification.toClassification(),
                    incident = incident.toIncident()
                )
            )
        }
    }

    return alerts
}
