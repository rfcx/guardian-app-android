package org.rfcx.incidents.entity.shortlink

import com.google.gson.annotations.SerializedName

data class ShortLinkRequest(
		@SerializedName("url")
		val url: String,
		@SerializedName("type")
		val type: String,
		@SerializedName("expires")
		val expires: String
)
