package org.rfcx.ranger.entity.user

import com.google.gson.annotations.SerializedName

data class InvitationCodeResponse(
		@SerializedName("success")
		val success: Boolean
)