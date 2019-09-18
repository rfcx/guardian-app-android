package org.rfcx.ranger.entity.user

import com.google.gson.annotations.SerializedName

class SetNameResponse (
		@SerializedName("given_name")
		val givenName: String
)
