package org.rfcx.ranger.util

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

enum class Screen(val id: String) {
	LOGIN("Login"),
	INVITECODE("InviteCode"),
	USERNAME("Username"),
	STATUS("Status"),
	ADDREPORT("AddReport"),
	REPORTDETAIL("ReportDetail"),
	MAP("Map"),
	ALERT("Alert"),
	ALERTGUARDIANDETAIL("AlertGuardianDetail"),
	AlertDetail("AlertDetail"),
	PROFILE("Profile"),
	FEEDBACK("Feedback")
}

class Analytics(context: Context) {
	private var firebaseAnalytics = FirebaseAnalytics.getInstance(context)
	private var context: Context? = context
	
	// region track screen
	fun trackScreen(screen: Screen) {
		firebaseAnalytics.setCurrentScreen(context as Activity, screen.id, null)
	}
	
	// region track event
	
	private fun trackEvent(eventName: String, params: Bundle) {
		firebaseAnalytics.setUserProperty("user_email", context?.getUserEmail())
		firebaseAnalytics.logEvent(eventName, params)
	}
	
	fun trackLoginEvent(method: String) {
		Log.d("", method)
		val bundle = Bundle()
		bundle.putString(FirebaseAnalytics.Param.METHOD, method)
		trackEvent(FirebaseAnalytics.Event.LOGIN, bundle)
	}
	
	fun trackEnterInviteCodeEvent(inviteCode: String) {
		val bundle = Bundle()
		bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, inviteCode)
		trackEvent("enter_invite_code", bundle)
	}
	
	fun trackSetUsernameEvent() {
		val bundle = Bundle()
		trackEvent("set_username", bundle)
	}
	
	fun trackStartToAddReportEvent() {
		val bundle = Bundle()
		trackEvent("add_report_start", bundle)
	}
	
	fun trackSubmitTheReportEvent() {
		val bundle = Bundle()
		trackEvent("add_report_submit", bundle)
	}
	
	fun trackLocationTracking(time: Long) {
		val bundle = Bundle()
		val status = when {
			time < 120 -> "tracking_ok"
			time in 121..599 -> "tracking_slow"
			else -> "tracking_veryslow" // > 600
		}
		bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, status)
		bundle.putLong(FirebaseAnalytics.Param.VALUE, time)
		trackEvent("add_location_tracking", bundle)
	}
	
	fun trackSetGuardianGroupStartEvent(screen: Screen) {
		val bundle = Bundle()
		bundle.putString(FirebaseAnalytics.Param.SOURCE, screen.toString())
		trackEvent("set_guardian_group_start", bundle)
	}
	
	fun trackSeeReportDetailEvent(reportId: String, reportName: String) {
		val bundle = Bundle()
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, reportId)
		bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, reportName)
		trackEvent("see_report_detail", bundle)
	}
	
	fun trackSeeAlertDetailEvent(alertId: String, alertName: String) {
		val bundle = Bundle()
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, alertId)
		bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, alertName)
		trackEvent("see_alert_detail", bundle)
	}
	
	fun trackReviewAlertEvent(alertId: String, alertName: String, review: String) {
		val bundle = Bundle()
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, alertId)
		bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, alertName)
		bundle.putString(FirebaseAnalytics.Param.VALUE, review)
		trackEvent("review_alert_detail", bundle)
	}
	
	fun trackFollowAlertEvent(alertId: String, alertName: String) {
		val bundle = Bundle()
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, alertId)
		bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, alertName)
		trackEvent("follow_alert_detail", bundle)
	}
	
	fun trackFeedbackStartEvent() {
		val bundle = Bundle()
		trackEvent("feedback_start", bundle)
	}
	
	fun trackRateAppEvent() {
		val bundle = Bundle()
		trackEvent("rate_app", bundle)
	}
	
	fun trackFeedbackSentEvent() {
		val bundle = Bundle()
		trackEvent("feedback_sent", bundle)
	}
	
	fun trackSetGuardianGroupEvent() {
		val bundle = Bundle()
		trackEvent("set_guardian_group", bundle)
	}
}