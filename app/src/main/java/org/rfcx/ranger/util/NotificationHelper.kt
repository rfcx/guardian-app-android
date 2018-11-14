package org.rfcx.ranger.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.message.Message
import org.rfcx.ranger.view.LoginActivity
import org.rfcx.ranger.view.MessageListActivity
import java.util.*

class NotificationHelper {
	
	companion object {
		@Volatile
		private var INSTANCE: NotificationHelper? = null
		
		fun getInstance(): NotificationHelper =
				INSTANCE ?: synchronized(this) {
					INSTANCE ?: NotificationHelper()
				}
		
		private const val NOTIFICATION_MESSAGE_NAME = "Message"
		private const val NOTIFICATION_MESSAGE_CHANNEL_ID = "Message"
		
		private const val NOTIFICATION_EVENT_NAME = "Event"
		private const val NOTIFICATION_EVENT_CHANNEL_ID = "Event"
		
		private const val NOTIFICATION_AUTHORIZE_ID = 13
		private const val NOTIFICATION_AUTHORIZE_NAME = "Authorize"
		private const val NOTIFICATION_AUTHORIZE_CHANNEL_ID = "Authorize"
	}
	
	fun showMessageNotification(context: Context, message: Message) {
		val intent = Intent(context, MessageListActivity::class.java)
		val pendingIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_ONE_SHOT)
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createMessageNotificationChannel(context)
		}
		
		val builder = notificationBuilderWithDefaults(context, NOTIFICATION_MESSAGE_CHANNEL_ID)
				.setSmallIcon(R.drawable.ic_notification)
				.setContentTitle(message.from?.firstname)
				.setContentText(message.text)
				.setContentIntent(pendingIntent)
		getNotificationManager(context).notify(Random().nextInt(10), builder.build())
	}
	
	@RequiresApi(Build.VERSION_CODES.O)
	private fun createMessageNotificationChannel(context: Context) {
		val channel = NotificationChannel(NOTIFICATION_MESSAGE_CHANNEL_ID, NOTIFICATION_MESSAGE_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
			setShowBadge(true)
			enableLights(true)
			enableVibration(true)
		}
		getNotificationManager(context).createNotificationChannel(channel)
	}
	
	fun showAlertNotification(context: Context, event: Event) {
		
		/*  val intent = Intent(android.content.Intent.ACTION_VIEW,
				  Uri.parse("http://maps.google.com/maps?q="
						  + message.coords?.lat + ","
						  + message.coords?.lon))
						  */
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createEventNotificationChannel(context)
		}
		
		val intent = Intent(context, MessageListActivity::class.java)
		val pendingIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_ONE_SHOT)
		
		val builder = notificationBuilderWithDefaults(context, NOTIFICATION_EVENT_CHANNEL_ID)
				.setSmallIcon(R.drawable.ic_alert_noti)
				.setContentTitle(event.value)
				.setContentText(event.guardianShortname + " " + DateHelper.getEventTime(event))
				.setContentIntent(pendingIntent)
		
		getNotificationManager(context).notify(Random().nextInt(10), builder.build())
	}
	
	@RequiresApi(Build.VERSION_CODES.O)
	private fun createEventNotificationChannel(context: Context) {
		val channel = NotificationChannel(NOTIFICATION_EVENT_CHANNEL_ID, NOTIFICATION_EVENT_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
			setShowBadge(true)
			enableLights(true)
			enableVibration(true)
		}
		getNotificationManager(context).createNotificationChannel(channel)
	}
	
	fun showLoginNotification(context: Context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createLogInNotificationChannel(context)
		}
		val openAppIntent = Intent(context, LoginActivity::class.java)
		openAppIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
		val openLoginPagePendingIntent = PendingIntent.getActivity(context, 0,
				openAppIntent, PendingIntent.FLAG_ONE_SHOT)
		
		val builder = notificationBuilderWithDefaults(context, NOTIFICATION_AUTHORIZE_CHANNEL_ID)
				.setSmallIcon(R.mipmap.ic_launcher)
				.setContentTitle(context.getString(R.string.app_name))
				.setContentText(context.getString(R.string.re_login))
				.setContentIntent(openLoginPagePendingIntent)
				.setOnlyAlertOnce(true)
		getNotificationManager(context).notify(NOTIFICATION_AUTHORIZE_ID, builder.build())
	}
	
	@RequiresApi(Build.VERSION_CODES.O)
	private fun createLogInNotificationChannel(context: Context) {
		val channel = NotificationChannel(NOTIFICATION_AUTHORIZE_CHANNEL_ID, NOTIFICATION_AUTHORIZE_NAME,
				NotificationManager.IMPORTANCE_HIGH).apply {
			setShowBadge(false)
			enableLights(true)
			enableVibration(true)
		}
		getNotificationManager(context).createNotificationChannel(channel)
	}
	
	
	private fun notificationBuilderWithDefaults(context: Context, channelId: String): NotificationCompat.Builder {
		return NotificationCompat.Builder(context, channelId)
				.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
				.setVibrate(longArrayOf(1000, 1000))
				.setAutoCancel(true)
	}
	
	private fun getNotificationManager(context: Context): NotificationManager {
		return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
	}
	
	
}