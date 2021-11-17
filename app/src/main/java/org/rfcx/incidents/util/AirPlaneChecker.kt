package org.rfcx.incidents.util

import android.content.Context
import android.provider.Settings

fun Context.isOnAirplaneMode(): Boolean {
	return Settings.Global.getInt(this.contentResolver,
			Settings.Global.AIRPLANE_MODE_ON, 0) != 0
}
