package org.rfcx.ranger.entity.report

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Report(
		val value: String, // e.g. chainsaw
		val site: String,
		@SerializedName("reported_at")
		val reportedAt: String,
		@SerializedName("lat")
		val latitude: Double,
		@SerializedName("long")
		val longitude: Double,
		@SerializedName("age_estimate")
		val ageEstimate: Int,
		@SerializedName("distance")
		val distanceEstimate: Int? = null, // unused on client
		@Expose(serialize = false)
		val audioLocation: String? = null // unused on server
)