package org.rfcx.incidents.util

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import org.rfcx.incidents.service.LocationTrackerService

/**
 * Keeps track of the status of location tracking
 */

class LocationTracking {
	
	companion object {
		
		val TRACKING_ON = "on"
		val TRACKING_OFF = "off"
		
		fun isOn(context: Context): Boolean {
			val preferences = Preferences.getInstance(context)
			val state = preferences.getString(Preferences.ENABLE_LOCATION_TRACKING, "")
			if (state.isEmpty()) {
				preferences.putString(Preferences.ENABLE_LOCATION_TRACKING, TRACKING_OFF)
				return false
			} else {
				return state == TRACKING_ON
			}
		}
		
		fun set(context: Context, on: Boolean) {
			val preferences = Preferences.getInstance(context)
			preferences.putString(Preferences.ENABLE_LOCATION_TRACKING, if (on) TRACKING_ON else TRACKING_OFF)
			updateService(context)
		}
		
		private fun updateService(context: Context) {
			if (isOn(context)) {
				ContextCompat.startForegroundService(context, Intent(context, LocationTrackerService::class.java))
			} else {
				context.stopService(Intent(context, LocationTrackerService::class.java))
			}
		}
	}
	
}
