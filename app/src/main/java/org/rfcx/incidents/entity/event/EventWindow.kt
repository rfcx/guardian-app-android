package org.rfcx.incidents.entity.event

import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class EventWindow : RealmModel {
	
	@PrimaryKey
	@SerializedName("guid")
	var guid: String = ""
	@SerializedName("confidence")
	var confidence: Double = 0.0
	@SerializedName("start")
	var start: Int = 0
	@SerializedName("end")
	var end: Int = 0
}
