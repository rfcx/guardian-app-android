package org.rfcx.ranger.entity.site

import com.google.gson.annotations.SerializedName

open class SiteResponse(
		@SerializedName("guid")
		val id: String,
		val name: String
)