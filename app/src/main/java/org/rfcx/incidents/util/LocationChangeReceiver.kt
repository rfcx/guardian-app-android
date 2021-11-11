package org.rfcx.incidents.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationResult

class LocationChangeReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context?, intent: Intent?) {
		if (LocationResult.hasResult(intent)) {
			val locationResult = LocationResult.extractResult(intent)
			val location = locationResult.lastLocation
			if (location != null) {
				Log.d("LocationChangeReceiver", "onReceive $location")
			}
		}
	}
}
