package org.rfcx.ranger.view.alert

interface AlertListener {
	fun showDetail(eventGuID: String)
	fun onReviewed(eventGuID: String, reviewValue: String)
}