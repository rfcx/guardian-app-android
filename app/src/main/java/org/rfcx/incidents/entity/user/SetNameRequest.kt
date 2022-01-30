package org.rfcx.incidents.entity.user

import com.google.gson.annotations.SerializedName

data class SetNameRequest(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("given_name")
    val givenName: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("nickname")
    val nickname: String
)
