package org.rfcx.incidents.util.socket

import org.rfcx.incidents.entity.guardian.socket.GuardianPing

object PingUtils {

    fun GuardianPing.getSoftware(): Map<String, String>? {
        val software = this.software ?: return null
        val softwareList = software.split("|")
        val mapSoftwareVersion = mutableMapOf<String, String>()
        softwareList.forEach {
            val role = it.split("*")[0]
            val version = it.split("*")[1]
            mapSoftwareVersion[role] = version
        }
        return mapSoftwareVersion
    }
}
