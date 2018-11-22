package org.rfcx.ranger.entity.location

import com.google.gson.annotations.SerializedName

data class CheckInResult(
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("id")
    val id: Any?,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?,
    @SerializedName("time")
    val time: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("user_id")
    val userId: Int?
)