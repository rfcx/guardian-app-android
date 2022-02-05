package org.rfcx.incidents.util

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.jsonwebtoken.Jwts
import io.realm.Realm
import org.rfcx.incidents.AppRealm
import org.rfcx.incidents.view.login.LoginActivity

fun Context.getTokenID(): String? {
    val idToken = Preferences.getInstance(this).getString(Preferences.ID_TOKEN, "")
    return if (idToken.isEmpty()) null else idToken
}

fun Context.getUserNickname(): String {
    val nickname = Preferences.getInstance(this).getString(Preferences.NICKNAME)
    return if (nickname != null && nickname.isNotEmpty()) nickname else "Responder"
}

fun Context?.logout() {
    this?.let {
        Preferences.getInstance(it).getArrayList(Preferences.SUBSCRIBED_PROJECTS)?.forEach { projectId ->
            CloudMessaging.unsubscribe(projectId)
        }
        this.removeLocationUpdates()
        Realm.getInstance(AppRealm.configuration()).use { realm ->
            realm.executeTransactionAsync({ bgRealm ->
                bgRealm.deleteAll()
            }, {
                realm.close()
                LoginActivity.startActivity(this)
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

    return loginWith
}

fun Context?.getUserProfile(): String? {
    val preferences = this?.let { Preferences.getInstance(it) }
    return preferences?.getString(Preferences.IMAGE_PROFILE)
}

