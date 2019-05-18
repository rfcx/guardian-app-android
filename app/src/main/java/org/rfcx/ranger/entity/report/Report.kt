package org.rfcx.ranger.entity.report

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Report(
		@PrimaryKey
		@Expose(serialize = false)
		var id: Int = 0,
		@Expose(serialize = false)
		var guid: String? = null,//  e.g. ec4dfe81-f10c-9592-b47a-14aada85112b
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
		var audioLocation: String? = null, // unused on server
		@Expose(serialize = false)
		var syncState: Int = 0 // local state: 0 unsent, 1 uploading, 2 uploaded (sync complete)
) : RealmObject() {

	companion object {
		const val FIELD_GUID = "guid"
		const val FIELD_ID = "id"
	}
}