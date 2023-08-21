package org.rfcx.incidents.entity.guardian.deployment

import org.rfcx.incidents.view.report.deployment.MapMarker
import java.util.Date

data class InfoWindowMarker(
    val id: Int,
    val locationName: String,
    val longitude: Double,
    val latitude: Double,
    val pin: String,
    val deploymentKey: String?,
    val createdAt: Date?,
    val deploymentAt: Date?,
    val isDeployment: Boolean
)

fun MapMarker.SiteMarker.toInfoWindowMarker(): InfoWindowMarker {
    return InfoWindowMarker(
        this.id,
        this.name,
        this.longitude,
        this.latitude,
        this.pin,
        null,
        null,
        null,
        false
    )
}

fun MapMarker.DeploymentMarker.toInfoWindowMarker(): InfoWindowMarker {
    return InfoWindowMarker(
        this.id,
        this.streamName,
        this.longitude,
        this.latitude,
        this.pin,
        this.deploymentKey,
        this.createdAt,
        this.deploymentAt,
        true
    )
}

