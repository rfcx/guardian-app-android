package org.rfcx.ranger.util

import android.content.Context
import android.content.Intent
import org.rfcx.ranger.data.local.WeeklySummaryData
import org.rfcx.ranger.service.LocationTrackerService

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
			val weeklySummaryData = WeeklySummaryData(Preferences(context))
			
			if (isOn(context)) {
				context.startService(Intent(context, LocationTrackerService::class.java))
				weeklySummaryData.startDutyTracking()
			} else {
				context.stopService(Intent(context, LocationTrackerService::class.java))
				weeklySummaryData.stopDutyTracking()
			}
		}
	}
	
}