package org.rfcx.ranger.util

import android.content.Context
import android.util.Log

fun Context.getTokenID(): String? {
	val idToken = Preferences.getInstance(this).getString(Preferences.ID_TOKEN, "")
	Log.d("getToken", idToken)
	return if (idToken.isEmpty()) null else idToken
}

fun Context.getSite(): String? {
	val site = Preferences.getInstance(this).getString(Preferences.DEFAULT_SITE, "")
	return if (site.isEmpty()) null else site
}

fun Context.getUserGuId(): String? {
	val guId = Preferences.getInstance(this).getString(Preferences.USER_GUID, "")
	return if (guId.isEmpty()) null else guId
}
