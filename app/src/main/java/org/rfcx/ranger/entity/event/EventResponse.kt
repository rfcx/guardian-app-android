package org.rfcx.ranger.entity.event

import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import java.util.*

data class EventResponse(
		@SerializedName("guid")
		val id: String,
		
		@SerializedName("audioGuid")
		val audioId: String,
		
		@SerializedName("urls")
		val audio: Audio,
		
		@SerializedName("latitude")
		val latitude: Double?,
		
		@SerializedName("longitude")
		val longitude: Double?,
		
		@SerializedName("audioMeasuredAt")
		val beginsAt: Date,
		
		@SerializedName("type")
		val type: String?,
		
		@SerializedName("value")
		val value: String = "",
		
		@SerializedName("label")
		val label: String = "",
		
		@SerializedName("confirmed")
		val confirmedCount: Int,
		
		@SerializedName("rejected")
		val rejectedCount: Int,
		
		@SerializedName("audioDuration")
		val audioDuration: Long,
		
		@SerializedName("guardianGuid")
		val guardianId: String,
		
		@SerializedName("guardianShortname")
		val guardianName: String,
		
		@SerializedName("siteGuid")
		val site: String,
		
		@SerializedName("windows")
		val windows: List<EventWindow>?,
		
		@SerializedName("review")
		val review: Review?,
		
		@SerializedName("last_review")
		val reviewer: Reviewer?

) {
	fun toEvent(): Event {
		val event = Event()
		event.id = id
		event.audioId = audioId
		event.latitude = latitude
		event.longitude = longitude
		event.beginsAt = beginsAt
		event.type = type
		event.value = value
		event.label = label
		event.confirmedCount = confirmedCount
		event.rejectedCount = rejectedCount
		event.audioDuration = audioDuration
		event.guardianId = guardianId
		event.guardianName = guardianName
		event.site = site
		event.audioOpusUrl = audio.opus
		event.audioPngUrl = audio.png
		if (windows != null) {
			event.windows.addAll(windows)
		}
		if (review != null) {
			event.reviewCreated = review.created
		}
		
		reviewer?.let {
			event.firstNameReviewer = it.firstName ?: it.email.split("@")[0]
			event.reviewConfirmed = it.confirmed
		}
		
		return event
	}
}