package org.rfcx.incidents.entity.user

import com.google.gson.annotations.SerializedName

data class InvitationCodeResponse(
    @SerializedName("success")
    val success: Boolean
)
