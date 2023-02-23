package org.rfcx.incidents.data.remote.guardian.software

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.Date

data class SoftwareResponse(
    @SerializedName("role") override val name: String,
    override val version: String,
    @Expose override val sha1: String,
    @Expose val size: Long,
    override val url: String,
    @Expose val released: Date
) : GuardianFileResponse
