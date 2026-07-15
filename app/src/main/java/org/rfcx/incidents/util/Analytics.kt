package org.rfcx.incidents.util

import android.content.Context
import android.os.Bundle
import com.posthog.PostHog

// Product analytics is self-hosted PostHog (track.rfcx.org) as of 2026-07-15,
// replacing Firebase Analytics. Firebase Crashlytics/Config/Messaging/Firestore/
// Storage stay. The public API of this class + every call site are unchanged;
// only the two backing methods send to PostHog now. Firebase reserved event/param
// names are preserved verbatim (login, item_name, ...) so the taxonomy is
// byte-identical — only the destination changed.

enum class Screen(val id: String) {
    LOGIN("Login"),
    USERNAME("Username"),
    MAP("Map"),
    PROFILE("Profile"),
    FEEDBACK("Feedback"),
    SUBSCRIBE_PROJECTS("SubscribeProjects"),
    NEW_EVENTS("NewEvents"),
    DRAFT_REPORTS("DraftReports"),
    SUBMITTED_REPORTS("SubmittedReports"),
    RESPONSE_DETAIL("ResponseDetail"),
    GUARDIAN_EVENT_DETAIL("GuardianEventDetail"),
    INVESTIGATION_TIMESTAMP("InvestigationTimestamp"),
    INVESTIGATION_TYPE("InvestigationType"),
    EVIDENCE("Evidence"),
    DAMAGE("Damage"),
    ACTION("Action"),
    ASSETS("Assets")
}

class Analytics(context: Context) {
    private var context: Context? = context

    // Firebase reserved event/param string values, preserved so the taxonomy is
    // byte-identical after moving off Firebase Analytics.
    private object FA {
        const val EVENT_LOGIN = "login"
        const val PARAM_METHOD = "method"
        const val PARAM_ITEM_NAME = "item_name"
        const val PARAM_ITEM_ID = "item_id"
        const val PARAM_SOURCE = "source"
        const val PARAM_VALUE = "value"
    }

    // Convert the existing Firebase Bundle params to the Map PostHog expects.
    private fun Bundle.toMap(): Map<String, Any> {
        val map = HashMap<String, Any>()
        for (key in keySet()) {
            @Suppress("DEPRECATION")
            get(key)?.let { map[key] = it }
        }
        return map
    }

    // region track screen
    fun trackScreen(screen: Screen) {
        // Manual screen capture (autocapture is off). PostHog's screen event.
        PostHog.screen(screen.id)
    }

    // region track event

    private fun trackEvent(eventName: String, params: Bundle) {
        // Identity = the signed-in user's email (matches the old user_email prop).
        context?.getUserEmail()?.takeIf { it.isNotEmpty() }?.let { PostHog.identify(it) }
        PostHog.capture(eventName, properties = params.toMap())
    }

    fun trackLoginEvent(method: String) {
        val bundle = Bundle()
        bundle.putString(FA.PARAM_METHOD, method)
        trackEvent(FA.EVENT_LOGIN, bundle)
    }

    fun trackEnterInviteCodeEvent(inviteCode: String) {
        val bundle = Bundle()
        bundle.putString(FA.PARAM_ITEM_NAME, inviteCode)
        trackEvent("enter_invite_code", bundle)
    }

    fun trackSetUsernameEvent() {
        val bundle = Bundle()
        trackEvent("set_username", bundle)
    }

    fun trackCreateResponseEvent() {
        val bundle = Bundle()
        trackEvent("create_response", bundle)
    }

    fun trackSubmitResponseEvent() {
        val bundle = Bundle()
        trackEvent("submit_response", bundle)
    }

    fun trackSaveDraftResponseEvent() {
        val bundle = Bundle()
        trackEvent("save_draft_response", bundle)
    }

    fun trackStartToAddReportEvent() {
        val bundle = Bundle()
        trackEvent("add_report_start", bundle)
    }

    fun trackSubmitTheReportEvent() {
        val bundle = Bundle()
        trackEvent("add_report_submit", bundle)
    }

    fun trackSatelliteCount(count: Int) {
        val bundle = Bundle()
        bundle.putString(FA.PARAM_VALUE, count.toString())
        trackEvent("satellite_count", bundle)
    }

    fun trackSetGuardianGroupStartEvent(screen: Screen) {
        val bundle = Bundle()
        bundle.putString(FA.PARAM_SOURCE, screen.toString())
        trackEvent("set_guardian_group_start", bundle)
    }

    fun trackSeeReportDetailEvent(reportId: String, reportName: String) {
        val bundle = Bundle()
        bundle.putString(FA.PARAM_ITEM_ID, reportId)
        bundle.putString(FA.PARAM_ITEM_NAME, reportName)
        trackEvent("see_report_detail", bundle)
    }

    fun trackSeeAlertDetailEvent(eventId: String, eventName: String) {
        val bundle = Bundle()
        bundle.putString(FA.PARAM_ITEM_ID, eventId)
        bundle.putString(FA.PARAM_ITEM_NAME, eventName)
        trackEvent("see_alert_detail", bundle)
    }

    fun trackReviewAlertEvent(eventId: String, eventName: String, review: String) {
        val bundle = Bundle()
        bundle.putString(FA.PARAM_ITEM_ID, eventId)
        bundle.putString(FA.PARAM_ITEM_NAME, eventName)
        bundle.putString(FA.PARAM_VALUE, review)
        trackEvent("review_alert_detail", bundle)
    }

    fun trackFollowAlertEvent(eventId: String, eventName: String) {
        val bundle = Bundle()
        bundle.putString(FA.PARAM_ITEM_ID, eventId)
        bundle.putString(FA.PARAM_ITEM_NAME, eventName)
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
