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

class NotificationDemo {
	fun startDemo(context: Context) {
		Handler().postDelayed({
			val intent = Intent(context, MainActivityNew::class.java)
			intent.putExtra(AlertNotification.ALERT_ID_NOTI_INTENT, "04ec88d8-04c6-4b88-9e9c-41d206724d0f")
			val stackBuilder = TaskStackBuilder.create(context)
			stackBuilder.addParentStack(MainActivityNew::class.java)
			stackBuilder.addNextIntent(intent)
			val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
			val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
			
			val notification = NotificationCompat.Builder(context, AlertNotification.NOTIFICATION_CHANNEL_ID)
					.setSmallIcon(R.mipmap.ic_launcher)
					.setSound(alarmSound)
					.setContentTitle("Rainforest Connection")
					.setContentText("Vehicle detected from Romania")
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
