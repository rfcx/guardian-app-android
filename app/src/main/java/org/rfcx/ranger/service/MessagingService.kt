package org.rfcx.ranger.service

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.rfcx.ranger.view.MainActivity
import android.media.RingtoneManager


class MessagingService : FirebaseMessagingService() {
	
	override fun onMessageReceived(remoteMessage: RemoteMessage?) {
		super.onMessageReceived(remoteMessage)

		// Play a sound
		if (remoteMessage != null && remoteMessage.notification != null && remoteMessage.notification!!.sound == "default") {
			try {
				val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
				val r = RingtoneManager.getRingtone(this, notification)
				r.play()
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}

		// Broadcast message to refresh
		val intent = Intent(MainActivity.INTENT_FILTER_MESSAGE_BROADCAST)
		applicationContext.sendBroadcast(intent)
	}
}