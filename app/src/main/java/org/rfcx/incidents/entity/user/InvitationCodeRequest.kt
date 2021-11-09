package org.rfcx.incidents.entity.user

import com.google.gson.annotations.SerializedName

data class InvitationCodeRequest(
		@SerializedName("code")
		val code: String,
		
		@SerializedName("accept_terms")
		val acceptTerms: String,
		
		@SerializedName("app")
		val app: String
)
