package org.rfcx.incidents.entity

import com.google.gson.annotations.SerializedName


data class ErrorResponse(
    val message: String, //invalid email or password
    val error: Error
)

data class Error(
    val status: Int //401
)

data class ErrorResponse2(
    @SerializedName("msg")
    val msg: ErrorResponse2Msg?
)

data class ErrorResponse2Msg(
    @SerializedName("message")
    val message: String?,
    @SerializedName("name")
    val name: String?
)
