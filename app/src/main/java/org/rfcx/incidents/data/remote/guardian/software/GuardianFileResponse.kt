package org.rfcx.incidents.data.remote.guardian.software

interface GuardianFileResponse {
    val name: String
    val version: String
    val url: String
    val sha1: String
}
