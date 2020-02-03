package org.rfcx.ranger.view.alert

import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.util.EventItem

interface AlertListener {
	fun showDetail(eventGuID: String, state: EventItem.State)
	fun onReviewed(reviewValue: String, event: Event)
}