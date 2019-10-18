package org.rfcx.ranger.entity.event

import com.google.gson.annotations.SerializedName
import java.util.*

data class EventsRequestFactory(
		val guardianInGroup: List<String>,
		val order: String,
		val dir: String,
		val limit: Int,
		val offset: Int)

data class EventsGuardianRequestFactory(
		val guardian: String,
		val value: String,
		val time: Date,
		val orderBy: String,
		val dir: String,
		val limit: Int,
		val offset: Int,
		val type: String)

/**
 * A wrapper class for review Event
 * @param eventGuID event guid
 * @param reviewConfirm is String 'confirm' or 'reject'
 */
data class ReviewEventFactory(val eventGuID: String, val reviewConfirm: String) {
	
	companion object {
		const val confirmEvent = "confirm"
		const val rejectEvent = "reject"
	}
}

data class ReviewEventRequest(
		@SerializedName("confirmed")
		val confirmed: Boolean,
		@SerializedName("unreliable")
		val unreliable: Boolean,
		@SerializedName("windows")
		val windows: ArrayList<String>)

class GuardianGroupFactory