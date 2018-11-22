package org.rfcx.ranger.util

import android.content.Context
import android.content.Intent
import org.rfcx.ranger.service.LocationTrackerService

/**
 * Keeps track of the status of location tracking
 */

class LocationTracking {

    companion object {

        private val TRACKING_ON = "on"
        private val TRACKING_OFF = "off"

        fun isOn(context: Context): Boolean {
            val preferences = Preferences.getInstance(context)
            val state = preferences.getString(Preferences.ENABLE_LOCATION_TRACKING, "")
            if (state.isEmpty()) {
                // state never setting before
                if (context.isLocationAllow()) {
                    // state on
                    preferences.putString(Preferences.ENABLE_LOCATION_TRACKING, TRACKING_ON)
                    return true
                } else {
                    preferences.putString(Preferences.ENABLE_LOCATION_TRACKING, TRACKING_OFF)
                    return false
                }
            } else {
                return state == TRACKING_ON
            }
        }

        fun set(context: Context, on: Boolean) {
            val preferences = Preferences.getInstance(context)
            preferences.putString(Preferences.ENABLE_LOCATION_TRACKING, if (on) TRACKING_ON else TRACKING_OFF)
            updateService(context)
        }

        fun updateService(context: Context) {
            if (isOn(context)) {
                context.startService(Intent(context, LocationTrackerService::class.java))
            }
            else {
                context.stopService(Intent(context, LocationTrackerService::class.java))
            }
        }
    }



}