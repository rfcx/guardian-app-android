package org.rfcx.ranger.entity.user

import com.google.gson.annotations.SerializedName

data class UserTouchResponse(
		@SerializedName("success")
		val success: Boolean
)