package org.rfcx.ranger.util

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.jsonwebtoken.Jwts
import io.realm.Realm
import org.rfcx.ranger.localdb.SiteGuardianDb
import org.rfcx.ranger.view.login.LoginActivityNew
import org.rfcx.ranger.view.profile.coordinates.CoordinatesActivity.Companion.DD_FORMAT

fun Context.getTokenID(): String? {
	val idToken = Preferences.getInstance(this).getString(Preferences.ID_TOKEN, "")
	Log.d("getToken", idToken)
	return if (idToken.isEmpty()) null else idToken
}

fun Context.getSiteName(): String {
	val defaultSiteName = Preferences.getInstance(this).getString(Preferences.DEFAULT_SITE, "")
	val database = SiteGuardianDb(Realm.getInstance(RealmHelper.migrationConfig()))
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

fun Context.getTimeZone(): String {
	return Preferences.getInstance(this).getString(Preferences.SITE_TIMEZONE, "")
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
		Realm.getInstance(RealmHelper.migrationConfig()).use { realm ->
			realm.executeTransactionAsync({ bgRealm ->
				bgRealm.deleteAll()
			}, {
				realm.close()
				LoginActivityNew.startActivity(this)
			}, {
				realm.close()
			})
		}
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
		FirebaseCrashlytics.getInstance().log(e.message.toString())
	}
	return userID
}

fun Context?.getUserEmail(): String {
	var userID = ""
	val token = this?.getTokenID()
	val withoutSignature = token?.substring(0, token.lastIndexOf('.') + 1)
	
	try {
		val untrusted = Jwts.parser().parseClaimsJwt(withoutSignature)
		userID = untrusted.body["email"] as String
	} catch (e: Exception) {
		e.printStackTrace()
		FirebaseCrashlytics.getInstance().log(e.message.toString())
	}
	return userID
}

fun Context?.saveUserLoginWith(): String {
	var loginWith = ""
	val token = this?.getTokenID()
	val withoutSignature = token?.substring(0, token.lastIndexOf('.') + 1)
	try {
		val untrusted = Jwts.parser().parseClaimsJwt(withoutSignature)
		loginWith = untrusted.body["sub"] as String
		loginWith = loginWith.split("|")[0]
	} catch (e: Exception) {
		e.printStackTrace()
		FirebaseCrashlytics.getInstance().log(e.message.toString())
	}
	
	val preferences = this?.let { Preferences.getInstance(it) }
	preferences?.putString(Preferences.LOGIN_WITH, loginWith)
	
	return loginWith
}

fun Context?.updateUserProfile(userProfile: String) {
	val preferences = this?.let { Preferences.getInstance(it) }
	preferences?.putString(Preferences.IMAGE_PROFILE, userProfile)
}

fun Context?.getUserProfile(): String? {
	val preferences = this?.let { Preferences.getInstance(it) }
	return preferences?.getString(Preferences.IMAGE_PROFILE)
}

fun Context?.getNameEmail(): String {
	return getUserEmail().split("@")[0]
}

fun Context?.getCoordinatesFormat(): String? {
	val preferences = this?.let { Preferences.getInstance(it) }
	return preferences?.getString(Preferences.COORDINATES_FORMAT, DD_FORMAT)
}
