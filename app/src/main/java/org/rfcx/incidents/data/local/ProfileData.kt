package org.rfcx.incidents.data.local

import io.realm.Realm
import org.rfcx.incidents.adapter.SyncInfo
import org.rfcx.incidents.entity.guardian.GuardianGroup
import org.rfcx.incidents.localdb.SiteGuardianDb
import org.rfcx.incidents.util.LocationTracking
import org.rfcx.incidents.util.Preferences
import org.rfcx.incidents.util.RealmHelper

class ProfileData(private val preferences: Preferences,private val guardianGroupDb: GuardianGroupDb) {
	
	fun getSiteName(): String {
		val defaultSiteName = preferences.getString(Preferences.DEFAULT_SITE, "")
		val database = SiteGuardianDb(Realm.getInstance(RealmHelper.migrationConfig()))
		val guardianGroupId = preferences.getString(Preferences.SELECTED_GUARDIAN_GROUP) ?: ""
		val siteId = database.guardianGroup(guardianGroupId)?.siteId ?: ""
		val site = database.site(siteId)
		return site?.name ?: defaultSiteName.capitalize()
	}
	
	fun getSiteId(): String {
		val defaultSiteName = preferences.getString(Preferences.DEFAULT_SITE, "")
		val database = SiteGuardianDb(Realm.getInstance(RealmHelper.migrationConfig()))
		val guardianGroupId = preferences.getString(Preferences.SELECTED_GUARDIAN_GROUP) ?: ""
		return database.guardianGroup(guardianGroupId)?.siteId ?: defaultSiteName
	}
	
	fun getDefaultSiteId(): String {
		return preferences.getString(Preferences.DEFAULT_SITE, "")
	}
	
	fun getUserNickname(): String {
		val nickname = preferences.getString(Preferences.NICKNAME)
		return if (nickname != null && nickname.isNotEmpty()) nickname.capitalize() else "${getSiteName()} Ranger"
	}
	
	fun getTracking(): Boolean {
		val tracking = preferences.getString(Preferences.ENABLE_LOCATION_TRACKING, LocationTracking.TRACKING_OFF)
		return tracking == LocationTracking.TRACKING_ON
	}
	
	fun getReceiveNotification(): Boolean {
		return preferences.getBoolean(Preferences.SHOULD_RECEIVE_EVENT_NOTIFICATIONS, true)
	}
	
	fun getReceiveNotificationByEmail(): Boolean {
		return preferences.getBoolean(Preferences.EMAIL_SUBSCRIBE, false)
	}
	
	fun updateReceivingNotification(received: Boolean) {
		preferences.putBoolean(Preferences.SHOULD_RECEIVE_EVENT_NOTIFICATIONS, received)
	}
	
	fun updateReceivingNotificationByEmail(received: Boolean) {
		preferences.putBoolean(Preferences.EMAIL_SUBSCRIBE, received)
	}
	
	fun hasGuardianGroup(): Boolean {
		val guardianGroup = preferences.getString(Preferences.SELECTED_GUARDIAN_GROUP, "")
		return guardianGroup.isNotEmpty()
	}
	
	fun setLastStatusSyncing(status: String) {
		preferences.putString(Preferences.LAST_STATUS_SYNCING, status)
	}
	
	fun getLastStatusSyncing(): String {
		return preferences.getString(Preferences.LAST_STATUS_SYNCING, SyncInfo.Status.UPLOADED.name)
	}
	
	fun getGuardianGroup(): GuardianGroup? {
		val group = preferences.getString(Preferences.SELECTED_GUARDIAN_GROUP, "")
		return if (group.isEmpty()) null else guardianGroupDb.getGuardianGroup(group)
	}
}