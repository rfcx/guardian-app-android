package org.rfcx.ranger.entity.event

data class EventsRequestFactory(
		val guardianGroup: String,
		val orderBy: String,
		val dir: String,
		val limit: Int,
		val offset: Int)

data class EventsGuardianRequestFactory(
		val groupList: List<String>,
		val orderBy: String,
		val dir: String,
		val limit: Int,
		val offset: Int)

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