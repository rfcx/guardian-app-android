package org.rfcx.incidents.view.report.deployment

import java.util.Date

sealed class MapMarker {
    data class SiteMarker(
        val id: Int,
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val altitude: Double,
        val pin: String
    ) : MapMarker()

    data class DeploymentMarker(
        val id: Int,
        val locationName: String,
        val longitude: Double,
        val latitude: Double,
        val pin: String,
        val description: String,
        val deploymentKey: String,
        val createdAt: Date,
        val deploymentAt: Date,
    ) : MapMarker()
}
