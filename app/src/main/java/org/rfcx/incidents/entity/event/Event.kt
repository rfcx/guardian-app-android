package org.rfcx.incidents.entity.event

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Event : RealmObject() {
	
	@PrimaryKey
	var id: String = ""
	var audioId: String = ""
	var latitude: Double? = null
	var longitude: Double? = null
	var beginsAt: Date = Date()
	var type: String? = ""
	var value: String = ""
	var label: String = ""
	var confirmedCount: Int = 0
	var rejectedCount: Int = 0
	var audioDuration: Long = 0
	var guardianId: String = ""
	var guardianName: String = ""
	var site: String = ""
	var audioOpusUrl: String = ""
	var audioPngUrl: String = ""
	var windows: RealmList<EventWindow> = RealmList()
	var reviewCreated: Date = Date()
	var reviewConfirmed: Boolean? = null
	var firstNameReviewer: String = ""
	
	companion object {
		// Event value
		const val chainsaw = "chainsaw"
		const val gunshot = "gunshot"
		const val vehicle = "vehicle"
		const val trespasser = "trespasser"
		const val other = "other"
		
	}
}
