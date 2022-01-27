package org.rfcx.incidents.entity.event

import com.google.gson.annotations.SerializedName


data class Confidence(
    @SerializedName("begins_at_offset")
    val beginsAtOffset: Long,
    @SerializedName("confidence")
    val confidence: Double,
    @SerializedName("ends_at_offset")
    val endsAtOffset: Long
)
