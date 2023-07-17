package org.rfcx.incidents.entity.guardian.socket

data class GuardianStorage(
    val internal: Storage?,
    val external: Storage?
)

data class Storage(
    val used: Long,
    val all: Long
)
