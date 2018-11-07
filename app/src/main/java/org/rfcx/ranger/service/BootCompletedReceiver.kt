package org.rfcx.ranger.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import org.rfcx.ranger.util.PrefKey
import org.rfcx.ranger.util.PreferenceHelper
import org.rfcx.ranger.view.SettingsActivity

class BootCompletedReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context?, intent: Intent?) {
		Toast.makeText(context, "ACTION_BOOT_COMPLETED", Toast.LENGTH_LONG).show()
		if (Intent.ACTION_BOOT_COMPLETED == intent?.action) {
			val locationTrackerService = Intent(context, LocationTrackerService::class.java)
			if (context != null && PreferenceHelper.getInstance(context).getString(PrefKey.ENABLE_LOCATION_TRACKING, "")
					!= SettingsActivity.TRACKING_OFF) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					context.startForegroundService(locationTrackerService)
				} else {
					context.startService(locationTrackerService)
				}
			}
		}
	}
}