package org.rfcx.ranger.util

import org.rfcx.ranger.localdb.SiteGuardianDb


fun Preferences.getSiteName(): String {
    val defaultSiteName = this.getString(Preferences.DEFAULT_SITE, "")
    val database = SiteGuardianDb()
    val guardianGroupId = this.getString(Preferences.SELECTED_GUARDIAN_GROUP) ?: ""
    val siteId = database.guardianGroup(guardianGroupId)?.siteId ?: ""
    val site = database.site(siteId)
    return if (site != null) site.name else defaultSiteName.capitalize()
}

fun Preferences.getGuardianGroup(): String? {
    val group = this.getString(Preferences.SELECTED_GUARDIAN_GROUP, "")
    return if (group.isEmpty()) null else group
}

fun Preferences.getUserGuId(): String? {
    val guId = this.getString(Preferences.USER_GUID, "")
    return if (guId.isEmpty()) null else guId
}

fun Preferences.getUserNickname(): String {
    val nickname = this.getString(Preferences.NICKNAME)
    return if (nickname != null && nickname.length > 0) nickname else "${getSiteName()} Ranger"
}

fun Preferences.isTracking(): Boolean {
    val tracking = this.getString(Preferences.ENABLE_LOCATION_TRACKING, LocationTracking.TRACKING_OFF)
    return tracking == LocationTracking.TRACKING_ON
}