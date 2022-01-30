package org.rfcx.incidents.util

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.jsonwebtoken.Jwts
import io.realm.Realm
import org.rfcx.incidents.view.login.LoginActivityNew
import org.rfcx.incidents.view.profile.coordinates.CoordinatesActivity.Companion.DD_FORMAT

fun Context.getTokenID(): String? {
    val idToken = Preferences.getInstance(this).getString(Preferences.ID_TOKEN, "")
    Log.d("getToken", idToken)
    return if (idToken.isEmpty()) null else idToken
}

fun Context.getUserNickname(): String {
    val nickname = Preferences.getInstance(this).getString(Preferences.NICKNAME)
    return if (nickname != null && nickname.length > 0) nickname else "Responder"
}

fun Context?.logout() {
    this?.let {
        val preferenceHelper = Preferences.getInstance(it)
        val projectCoreIds = preferenceHelper.getArrayList(Preferences.SUBSCRIBED_PROJECTS)
        projectCoreIds?.forEach { coreId ->
            CloudMessaging.unsubscribe(coreId)
        }
        this.removeLocationUpdates()
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
        Preferences.getInstance(this).clear()
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

fun Context?.getCoordinatesFormat(): String? {
    val preferences = this?.let { Preferences.getInstance(it) }
    return preferences?.getString(Preferences.COORDINATES_FORMAT, DD_FORMAT)
}
