package org.rfcx.incidents.entity.user

import com.google.gson.annotations.SerializedName

open class TermsRequest(
    val app: String
)

open class TermsResponse(
    @SerializedName("success")
    val success: Boolean
)
