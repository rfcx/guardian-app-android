package org.rfcx.incidents.data.remote.guardian.software

import com.google.gson.annotations.Expose
import java.util.Date

data class SoftwareResponse(
    val role: String,
    val version: String,
    val sha1: String,
    val size: Long,
    val url: String,
    val released: Date
)
