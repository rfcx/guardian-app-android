package org.rfcx.ranger.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.R
import org.rfcx.ranger.util.PrefKey
import org.rfcx.ranger.util.PreferenceHelper
import org.rfcx.ranger.util.RemoteConfigKey
import org.rfcx.ranger.view.SettingActivity

class BootCompletedReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context?, intent: Intent?) {
		Toast.makeText(context, "ACTION_BOOT_COMPLETED", Toast.LENGTH_LONG).show()
		if (Intent.ACTION_BOOT_COMPLETED == intent?.action) {
			val locationTrackerService = Intent(context, LocationTrackerService::class.java)
			if (context != null && PreferenceHelper.getInstance(context).getString(PrefKey.ENABLE_LOCATION_TRACKING, "")
					!= SettingActivity.TRACKING_OFF) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					context.startForegroundService(locationTrackerService)
				} else {
					context.startService(locationTrackerService)
				}
			}
			
			val rangerRemote = FirebaseRemoteConfig.getInstance()
			// config for debug
			val configSettings = FirebaseRemoteConfigSettings.Builder()
					.setDeveloperModeEnabled(BuildConfig.DEBUG)
					.build()
			
			rangerRemote.setConfigSettings(configSettings)
			rangerRemote.setDefaults(R.xml.ranger_remote_config_defualt)
			
			PullingAlertMessageReceiver.startAlarmForMessageNotification(context,
					rangerRemote.getLong(RemoteConfigKey.REMOTE_NOTI_FREQUENCY_DURATION))
		}
	}
}