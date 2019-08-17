package org.rfcx.ranger.view.alert

interface ReviewAlertCallback {
	fun onReviewed(eventGuID: String, reviewValue: String)
}