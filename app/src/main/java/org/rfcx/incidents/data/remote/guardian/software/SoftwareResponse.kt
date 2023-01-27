package org.rfcx.incidents.data.remote.guardian.software

import com.google.gson.annotations.SerializedName
import org.rfcx.incidents.entity.guardian.FileResponse
import java.util.Date

data class SoftwareResponse(
    @SerializedName("role")
    val role: String,
    @SerializedName("version")
    val version: String,
    @SerializedName("sha1")
    val sha1: String,
    @SerializedName("size")
    val size: Long,
    @SerializedName("url")
    val url: String,
    @SerializedName("released")
    val released: Date
) : FileResponse
