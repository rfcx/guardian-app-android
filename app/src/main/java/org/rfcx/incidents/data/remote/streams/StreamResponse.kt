package org.rfcx.incidents.data.remote.streams

import io.realm.RealmList
import org.rfcx.incidents.data.remote.guardian.deploy.DeploymentsResponse
import org.rfcx.incidents.entity.event.Classification
import org.rfcx.incidents.entity.event.Event
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.entity.stream.Incident
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.entity.stream.User
import java.util.Date

data class StreamResponse(
    var id: String,
    var name: String,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var project: ProjectResponse = ProjectResponse(),
    var timezone: String,
    var tags: List<String>,
    var incidents: IncidentListResponse = IncidentListResponse(),
    var guardianType: String
) {
    fun lastIncident(): IncidentResponse? = incidents.items.firstOrNull()
}

data class StreamDeviceAPIResponse(
    var id: String,
    var name: String,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var altitude: Double = 0.0,
    var project: ProjectResponse = ProjectResponse(),
    var deployment: DeploymentsResponse? = null
)

data class IncidentListResponse(
    var total: Int = 0,
    var items: List<IncidentResponse> = listOf()
)

data class IncidentResponse(
    var id: String,
    var ref: Int = 0,
    var closedAt: Date?,
    var createdAt: Date,
    var updatedAt: Date,
    var firstEventId: String,
    var events: List<EventResponse> = listOf(),
    var responses: List<ResponseItemResponse> = listOf()
)

data class EventResponse(
    var id: String,
    var start: Date,
    var end: Date,
    var createdAt: Date,
    var classification: ClassificationResponse
)

data class ResponseItemResponse(
    var id: String = "",
    var createdBy: UserResponse? = null
)

data class UserResponse(
    var firstname: String = ""
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
    name = name,
    latitude = latitude,
    longitude = longitude,
    timezoneRaw = timezone,
    projectId = project.id,
    tags = realmList(tags),
    lastIncident = lastIncident()?.toIncident(),
    guardianType = guardianType,
    externalId = id,
    syncState = SyncState.SENT.value
)

fun StreamDeviceAPIResponse.toStream(): Stream = Stream(
    name = name,
    latitude = latitude,
    longitude = longitude,
    altitude = altitude,
    projectId = project.id,
    externalId = id,
    syncState = SyncState.SENT.value,
    deployment = deployment?.toDeployment()
)

private fun IncidentResponse.toIncident(): Incident = Incident(
    id = this.id,
    ref = this.ref.toString(),
    closedAt = this.closedAt,
    createdAt = this.createdAt,
    responses = realmList(this.responses.map { User(it.createdBy?.firstname ?: "") })
)

fun EventResponse.toEvent(streamId: String): Event = Event(
    id = this.id,
    start = this.start,
    streamId = streamId,
    end = this.end,
    name = this.id,
    createdAt = this.createdAt,
    classification = this.classification.toClassification()
)

private fun ClassificationResponse.toClassification(): Classification = Classification(
    value = this.value,
    title = this.title
)

fun <T> realmList(list: List<T>): RealmList<T> {
    val result = RealmList<T>()
    list.forEach { result.add(it) }
    return result
}
