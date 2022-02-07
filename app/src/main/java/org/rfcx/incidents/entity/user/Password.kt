package org.rfcx.incidents.entity.user

import com.google.gson.annotations.SerializedName

open class PasswordRequest(
    @SerializedName("password")
    val password: String
)

open class PasswordResponse(
    @SerializedName("success")
    val success: Boolean
)
