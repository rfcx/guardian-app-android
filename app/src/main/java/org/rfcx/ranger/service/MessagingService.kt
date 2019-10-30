package org.rfcx.ranger.service

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.rfcx.ranger.service.AlertNotification.createAlert


class MessagingService : FirebaseMessagingService() {
	
	override fun onMessageReceived(remoteMessage: RemoteMessage) {
		
		if (remoteMessage.notification == null) return
		Log.i("MessagingService", "-- " + remoteMessage.data.toString())
		if (remoteMessage.data.containsKey("id")) {
			val alertNotification = createAlert(this, getNotificationManager()
					, remoteMessage.notification!!, remoteMessage.data)
			notify(createNotificationID(), alertNotification)
			Log.d("MessagingService", remoteMessage.data.toString())
		} else {
			// Play a sound
			super.onMessageReceived(remoteMessage)
			if (remoteMessage.notification != null && remoteMessage.notification!!.sound == "default") {
				try {
					val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
					val r = RingtoneManager.getRingtone(this, notification)
					r.play()
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
		}
		
		// Broadcast message to refresh
//		val intent = Intent(MainActivity.INTENT_FILTER_MESSAGE_BROADCAST)
//		applicationContext.sendBroadcast(intent)
	}
	
	
	private fun notify(id: Int, notification: Notification) {
		Log.w("MessagingService", "notify")
		getNotificationManager().notify(id, notification)
	}
	
	private fun getNotificationManager(): NotificationManager {
		return getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
	}
	
	private fun createNotificationID(): Int {
		return System.currentTimeMillis().toInt()
	}
}