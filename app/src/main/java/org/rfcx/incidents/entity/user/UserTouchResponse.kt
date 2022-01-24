package org.rfcx.incidents.entity.user

import com.google.gson.annotations.SerializedName

data class UserTouchResponse(
		@SerializedName("success")
		val success: Boolean
)
