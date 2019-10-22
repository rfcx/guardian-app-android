package org.rfcx.ranger.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.crashlytics.android.core.CrashlyticsCore
import com.google.firebase.messaging.RemoteMessage
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Audio
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.view.MainActivityNew

object AlertNotification {
	
	fun createAlert(context: Context, notificationManager: NotificationManager, notification: RemoteMessage.Notification,
	                data: Map<String, String>): Notification {
		
		val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
		
		val intent = Intent(context, MainActivityNew::class.java)
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
		intent.putExtra(ALERT_NOTI_INTENT, createEvent(data))
		
		val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
		val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
				.setAutoCancel(true)
				.setContentTitle(notification.title)
				.setContentText(notification.body)
				.setContentIntent(pendingIntent)
				.setSmallIcon(R.drawable.ic_notification)
				.setSound(defaultSoundUri)
				//.setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
				.setDefaults(Notification.DEFAULT_VIBRATE)
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel = NotificationChannel(
					NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
			)
			channel.description = NOTIFICATION_CHANNEL_DESCRIPTION
			channel.setShowBadge(true)
			channel.enableLights(true)
			channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
			channel.enableVibration(true)
			notificationManager.createNotificationChannel(channel)
		}
		return notificationBuilder.build()
	}
	
	private fun createEvent(data: Map<String, String>): Event {
		val event = Event()
		event.apply {
			event_guid = data["event_guid"] ?: ""
			audioGUID = data["audio_guid"]
			try {
				longitude = data["longitude"]?.toDouble()
				latitude = data["latitude"]?.toDouble()
			} catch (e: Exception) {
				e.printStackTrace()
				CrashlyticsCore.getInstance().logException(e)
			}
			
			value = data["value"]
			guardianGUID = data["guardian_guid"]
			guardianShortname = data["guardian_shortname"]
			type = data["type"]
			site = data["site_guid"]
			aiGuid = data["ai_guid"]
			
			audio = Audio().apply {
				opus = "https://assets.rfcx.org/audio/$audioGUID.opus"
			}
			
			reviewerConfirmed = false
		}
		return event
	}
	
	private const val NOTIFICATION_CHANNEL_ID = "Ranger Alert"
	private const val NOTIFICATION_CHANNEL_NAME = "Alert"
	private const val NOTIFICATION_CHANNEL_DESCRIPTION = "Alert"
	const val ALERT_NOTI_INTENT = "ALERT_NOTI_INTENT"
}