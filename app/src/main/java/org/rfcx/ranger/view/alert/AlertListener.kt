package org.rfcx.ranger.view.alert

import org.rfcx.ranger.entity.event.Event

interface AlertListener {
	fun showDetail(event: Event)
	fun onReviewed(eventGuID: String, reviewValue: String)
}