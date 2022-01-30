package org.rfcx.incidents.entity.report

import com.google.gson.annotations.SerializedName

data class UploadImageResponse(
    @SerializedName("guid")
    val guid: String?,
    @SerializedName("reported_at")
    val reportedAt: String?,
    @SerializedName("reporter")
    val reporter: Reporter?,
    @SerializedName("type")
    val type: String?,
    @SerializedName("url")
    val url: String?
)

data class Reporter(
    @SerializedName("email")
    val email: String?,
    @SerializedName("firstname")
    val firstname: String?,
    @SerializedName("guid")
    val guid: String?,
    @SerializedName("lastname")
    val lastname: String?
)
