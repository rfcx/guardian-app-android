package org.rfcx.ranger.data.local

import org.rfcx.ranger.localdb.SiteGuardianDb
import org.rfcx.ranger.util.*

class ProfileData(private val preferences: Preferences) {
	
	fun getSiteName(): String {
		val defaultSiteName = preferences.getString(Preferences.DEFAULT_SITE, "")
		val database = SiteGuardianDb()
		val guardianGroupId = preferences.getString(Preferences.SELECTED_GUARDIAN_GROUP) ?: ""
		val siteId = database.guardianGroup(guardianGroupId)?.siteId ?: ""
		val site = database.site(siteId)
		return if (site != null) site.name else defaultSiteName.capitalize()
	}

	fun getUserNickname(): String {
		val nickname = preferences.getString(Preferences.NICKNAME)
		return if (nickname != null && nickname.length > 0) nickname else "${getSiteName()} Ranger"
	}
	
	fun getTracking(): Boolean {
		val tracking = preferences.getString(Preferences.ENABLE_LOCATION_TRACKING, LocationTracking.TRACKING_OFF)
		return tracking == LocationTracking.TRACKING_ON
	}
	
	fun updateTracking(enable: Boolean) {
		preferences.putString(Preferences.ENABLE_LOCATION_TRACKING, if (enable) LocationTracking.TRACKING_ON else LocationTracking.TRACKING_OFF)
	}
	
	fun getReceiveNotification(): Boolean {
		return preferences.getBoolean(Preferences.SHOULD_RECEIVE_EVENT_NOTIFICATIONS)
	}
	
	fun updateReceivingNotification(received: Boolean) {
		preferences.putBoolean(Preferences.SHOULD_RECEIVE_EVENT_NOTIFICATIONS, received)
	}
}