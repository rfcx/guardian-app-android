package org.rfcx.incidents.entity.user

import com.google.gson.annotations.SerializedName

open class SubscribeRequest(
    val groups: List<String>
)

open class SubscribeResponse(
    @SerializedName("success")
    val success: Boolean
)
