package org.rfcx.ranger.util

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import androidx.core.app.NotificationCompat
import org.rfcx.ranger.R
import org.rfcx.ranger.service.AlertNotification
import org.rfcx.ranger.view.MainActivityNew

class NotificationDemo(private val guid: String?) {
	fun startDemo(context: Context) {
		Handler().postDelayed({
			val intent = Intent(context, MainActivityNew::class.java)
			intent.putExtra(AlertNotification.ALERT_ID_NOTI_INTENT, guid ?: "0ebcc9be-3222-4ae8-aa08-b023f215394d")
			val stackBuilder = TaskStackBuilder.create(context)
			stackBuilder.addParentStack(MainActivityNew::class.java)
			stackBuilder.addNextIntent(intent)
			val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
			val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
			
			val notification = NotificationCompat.Builder(context, AlertNotification.NOTIFICATION_CHANNEL_ID)
					.setSmallIcon(R.mipmap.ic_launcher)
					.setSound(alarmSound)
					.setContentTitle("Rainforest Connection")
					.setContentText("Chainsaw detected at Bear Hut #2")
					.setAutoCancel(true)
					.setContentIntent(pendingIntent)
					.setSmallIcon(R.drawable.ic_notification)
					.setDefaults(Notification.DEFAULT_VIBRATE)
					.setPriority(NotificationCompat.PRIORITY_HIGH)
					.build()
			
			val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				val channel = NotificationChannel(
						AlertNotification.NOTIFICATION_CHANNEL_ID, AlertNotification.NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
				)
				channel.description = AlertNotification.NOTIFICATION_CHANNEL_DESCRIPTION
				channel.setShowBadge(true)
				channel.enableLights(true)
				channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
				channel.enableVibration(true)
				notificationManager.createNotificationChannel(channel)
			}
			notificationManager.notify(1000, notification)
		}, 20000)
	}
}
