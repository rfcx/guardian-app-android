package org.rfcx.incidents.entity.location

import com.google.gson.annotations.SerializedName

data class CheckInRequest(
    @SerializedName("locations")
    val checkIns: List<CheckIn>?
)
