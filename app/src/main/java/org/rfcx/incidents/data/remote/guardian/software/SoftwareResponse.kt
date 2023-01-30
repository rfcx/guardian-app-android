package org.rfcx.incidents.data.remote.guardian.software

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.Date

data class SoftwareResponse(
    @Expose(serialize = false)
    val role: String,
    @Expose(serialize = false)
    val version: String,
    @Expose(serialize = false)
    val sha1: String,
    val size: Long,
    val url: String,
    @Expose(serialize = false)
    val released: Date
)
