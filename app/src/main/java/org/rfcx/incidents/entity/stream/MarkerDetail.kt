package org.rfcx.incidents.entity.stream

data class MarkerDetail(
    val id: Int,
    val name: String,
    val serverId: String,
    val distance: Double,
    val countEvents: Int
)
