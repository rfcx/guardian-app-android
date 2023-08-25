package org.rfcx.incidents.entity.stream

import org.rfcx.incidents.entity.guardian.deployment.InfoWindowMarker

data class MarkerDetail(
    val id: Int,
    val name: String,
    val serverId: String,
    val distance: Double,
    val countEvents: Int,
    val fromDeployment: Boolean = false,
    val infoWindowMarker: InfoWindowMarker? = null
)
