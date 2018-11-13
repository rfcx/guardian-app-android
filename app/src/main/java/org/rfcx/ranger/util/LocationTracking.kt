package org.rfcx.ranger.util

import android.content.Context
import android.content.Intent
import kotlinx.android.synthetic.main.activity_settings.*
import org.rfcx.ranger.service.LocationTrackerService
import org.rfcx.ranger.view.SettingsActivity

/**
 * Keeps track of the status of location tracking
 */

class LocationTracking {

    companion object {

        private val TRACKING_ON = "on"
        private val TRACKING_OFF = "off"

        fun isOn(context: Context): Boolean {
            val preferences = PreferenceHelper.getInstance(context)
            val state = preferences.getString(PrefKey.ENABLE_LOCATION_TRACKING, "")
            if (state.isEmpty()) {
                // state never setting before
                if (context.isLocationAllow()) {
                    // state on
                    preferences.putString(PrefKey.ENABLE_LOCATION_TRACKING, TRACKING_ON)
                    return true
                } else {
                    preferences.putString(PrefKey.ENABLE_LOCATION_TRACKING, TRACKING_OFF)
                    return false
                }
            } else {
                return state == TRACKING_ON
            }
        }

        fun set(context: Context, on: Boolean) {
            val preferences = PreferenceHelper.getInstance(context)
            preferences.putString(PrefKey.ENABLE_LOCATION_TRACKING, if (on) TRACKING_ON else TRACKING_OFF)
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