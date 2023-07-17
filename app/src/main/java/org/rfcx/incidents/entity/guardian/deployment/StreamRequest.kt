package org.rfcx.incidents.entity.guardian.deployment

import org.rfcx.incidents.entity.stream.Stream

data class StreamRequest(
    var name: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var altitude: Double = 0.0,
    var project: ProjectRequest? = null,
    var id: String? = null
)

fun Stream.toRequestBody(): StreamRequest {
    return StreamRequest(
        name = this.name,
        latitude = this.latitude,
        longitude = this.longitude,
        altitude = this.altitude,
        project = ProjectRequest(id = this.projectId),
        id = this.externalId
    )
}
