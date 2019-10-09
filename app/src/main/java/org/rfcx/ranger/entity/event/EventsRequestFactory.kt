package org.rfcx.ranger.entity.event

data class EventsRequestFactory(
		val guardianGroup: String,
		val orderBy: String,
		val dir: String,
		val limit: Int,
		val offset: Int)

data class EventsGuardianRequestFactory(
		val guardian: String,
		val value: String,
		val time: String,
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

class GuardianGroupFactory