package org.rfcx.incidents.entity.guardian.socket

data class SpeedTest(
    val downloadSpeed: Double,
    val uploadSpeed: Double,
    val isFailed: Boolean,
    val isTesting: Boolean,
    val hasConnection: Boolean
)
