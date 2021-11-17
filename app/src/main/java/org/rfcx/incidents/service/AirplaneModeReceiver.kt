package org.rfcx.incidents.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AirplaneModeReceiver(val callback: ((Boolean) -> Unit)?) : BroadcastReceiver() {
	
	override fun onReceive(context: Context?, intent: Intent?) {
		Log.d("AirplaneModeReceiver", "${intent?.getBooleanExtra("state",false)}")
		callback?.invoke(intent?.getBooleanExtra("state", false) ?: false)
	}
}
