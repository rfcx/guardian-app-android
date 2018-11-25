package org.rfcx.ranger.entity.report

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Report(
		@PrimaryKey
		@Expose(serialize = false)
		var id: Int = 0,
		@SerializedName("value")
		var value: String = "", // e.g. chainsaw
		@SerializedName("site")
		var site: String = "",
		@SerializedName("reported_at")
		var reportedAt: String = "",
		@SerializedName("lat")
		var latitude: Double = 0.0,
		@SerializedName("long")
		var longitude: Double = 0.0,
		@SerializedName("age_estimate")
		var ageEstimate: Int = 0,
		@SerializedName("distance")
		var distanceEstimate: Int? = null, // unused on client
		@Expose(serialize = false)
		var audioLocation: String? = null // unused on server
) : RealmObject()