package org.rfcx.ranger.entity.location

import com.google.gson.annotations.SerializedName

data class CheckIn(
		@SerializedName("locations")
		val locations: List<RangerLocation>?)