package org.rfcx.incidents.entity.guardian.deployment

data class DeviceParameter(
    val guid: String?,
    val guardianToken: String?,
    val ping: String?
)

data class SongMeterParameters(
    val songMeterPrefixes: String?
)
