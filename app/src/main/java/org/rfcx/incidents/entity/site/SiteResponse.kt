package org.rfcx.incidents.entity.site

import com.google.gson.annotations.SerializedName

open class SiteResponse(
		@SerializedName("guid")
		val id: String,
		val name: String,
		val bounds: Bounds,
		val timezone: String
)

open class Bounds(
		val type: String,
		val coordinates: Array<Array<Array<Array<Double>>>>
)
