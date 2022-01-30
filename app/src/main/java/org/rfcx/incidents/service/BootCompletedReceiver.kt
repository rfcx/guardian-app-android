package org.rfcx.incidents.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && Intent.ACTION_BOOT_COMPLETED == intent?.action) {
//			LocationTracking.updateService(context)
        }
    }
}
