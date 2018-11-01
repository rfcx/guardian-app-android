package org.rfcx.ranger.util

import android.content.Context
import android.util.Log


fun Context.getTokenID(): String? {
	val idToken = PreferenceHelper.getInstance(this).getString(PrefKey.ID_TOKEN, "")
	Log.d("getToken", idToken)
	return if (idToken.isEmpty()) null else idToken
}

fun Context.getToken(): String? {
	val idToken = PreferenceHelper.getInstance(this).getString(PrefKey.ACCESS_TOKEN, "")
	Log.d("getToken", idToken)
	return if (idToken.isEmpty()) null else idToken
}

fun Context.getSite(): String? {
	val site = PreferenceHelper.getInstance(this).getString(PrefKey.DEFAULT_SITE, "")
	return if (site.isEmpty()) null else site
}

fun Context.getUserGuId(): String? {
	val guId = PreferenceHelper.getInstance(this).getString(PrefKey.GU_ID, "")
	return if (guId.isEmpty()) null else guId
}

fun Context.getEmail(): String? {
	val email = PreferenceHelper.getInstance(this).getString(PrefKey.EMAIL, "")
	return if (email.isEmpty()) null else email
}
