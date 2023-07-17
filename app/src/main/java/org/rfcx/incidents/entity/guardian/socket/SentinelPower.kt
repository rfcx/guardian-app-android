package org.rfcx.incidents.entity.guardian.socket

data class SentinelPower(
    val input: SentinelInput = SentinelInput(),
    val system: SentinelSystem = SentinelSystem(),
    val battery: SentinelBattery = SentinelBattery()
)

data class SentinelInput(
    val voltage: Int = 0,
    val current: Int = 0,
    val misc: Int = 0,
    val power: Int = 0
)

data class SentinelSystem(
    val voltage: Int = 0,
    val current: Int = 0,
    val temp: Int = 0,
    val power: Int = 0
)

data class SentinelBattery(
    val voltage: Int = 0,
    val current: Int = 0,
    val percentage: Double = 0.0,
    val power: Int = 0
)
