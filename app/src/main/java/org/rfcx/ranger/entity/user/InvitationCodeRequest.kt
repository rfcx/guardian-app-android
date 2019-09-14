package org.rfcx.ranger.entity.user

import com.google.gson.annotations.SerializedName

data class InvitationCodeRequest(
		@SerializedName("code")
		val code: String
)