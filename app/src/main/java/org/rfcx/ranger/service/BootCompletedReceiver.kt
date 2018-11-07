package org.rfcx.ranger.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import org.rfcx.ranger.util.LocationTracking
import org.rfcx.ranger.util.PrefKey
import org.rfcx.ranger.util.PreferenceHelper
import org.rfcx.ranger.view.SettingsActivity

class BootCompletedReceiver : BroadcastReceiver() {

	override fun onReceive(context: Context?, intent: Intent?) {
		if (context != null && Intent.ACTION_BOOT_COMPLETED == intent?.action) {
			LocationTracking.updateService(context)
		}
	}
}