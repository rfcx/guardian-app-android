package org.rfcx.ranger.util

import android.content.Context
import android.util.Log
import com.crashlytics.android.Crashlytics
import io.jsonwebtoken.Jwts
import io.realm.Realm
import org.rfcx.ranger.localdb.SiteGuardianDb
import org.rfcx.ranger.view.login.LoginActivityNew

fun Context.getTokenID(): String? {
	val idToken = Preferences.getInstance(this).getString(Preferences.ID_TOKEN, "")
	Log.d("getToken", idToken)
	return if (idToken.isEmpty()) null else idToken
}

fun Context.getSiteName(): String {
	val defaultSiteName = Preferences.getInstance(this).getString(Preferences.DEFAULT_SITE, "")
	val database = SiteGuardianDb()
	val guardianGroupId = Preferences.getInstance(this).getString(Preferences.SELECTED_GUARDIAN_GROUP)
			?: ""
	val siteId = database.guardianGroup(guardianGroupId)?.siteId ?: ""
	val site = database.site(siteId)
	return if (site != null) site.name else defaultSiteName.capitalize()
}

fun Context.getGuardianGroup(): String? {
	val group = Preferences.getInstance(this).getString(Preferences.SELECTED_GUARDIAN_GROUP, "")
	return if (group.isEmpty()) null else group
}

fun Context.getUserGuId(): String? {
	val guId = Preferences.getInstance(this).getString(Preferences.USER_GUID, "")
	return if (guId.isEmpty()) null else guId
}

fun Context.getUserNickname(): String {
	val nickname = Preferences.getInstance(this).getString(Preferences.NICKNAME)
	return if (nickname != null && nickname.length > 0) nickname else "${getSiteName()} Ranger"
}

fun Preferences.getTokenID(): String? {
	val idToken = this.getString(Preferences.ID_TOKEN, "")
	Log.d("getToken", idToken)
	return if (idToken.isEmpty()) null else idToken
}

fun Context?.logout() {
	this?.let {
		CloudMessaging.unsubscribe(this)
		Preferences.getInstance(this).clear()
		LocationTracking.set(this, false)
		Realm.getDefaultInstance().use { realm ->
			realm.executeTransaction {
				it.deleteAll()
			}
		}
		LoginActivityNew.startActivity(this)
	}
}

fun Context?.getUserId(): String {
	var userID = ""
	val token = this?.getTokenID()
	val withoutSignature = token?.substring(0, token.lastIndexOf('.') + 1)
	try {
		val untrusted = Jwts.parser().parseClaimsJwt(withoutSignature)
		userID = untrusted.body["sub"] as String
	} catch (e: Exception) {
		e.printStackTrace()
		Crashlytics.logException(e)
	}
	return userID
}
