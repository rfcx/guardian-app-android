package org.rfcx.incidents.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import org.rfcx.incidents.R
import org.rfcx.incidents.view.MainActivity

object AlertNotification {

    fun createAlert(
        context: Context,
        notificationManager: NotificationManager,
        notification: RemoteMessage.Notification,
        data: Map<String, String>
    ): Notification {

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(INTENT_KEY_STREAM_ID, data["streamId"]) // TODO Test that the streamId exists

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(true)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_notification)
            .setSound(defaultSoundUri)
            // .setStyle(NotificationCompat.BigTextStyle().bigText(notification.body))
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

    const val NOTIFICATION_CHANNEL_ID = "Ranger Alert"
    const val NOTIFICATION_CHANNEL_NAME = "Alert"
    const val NOTIFICATION_CHANNEL_DESCRIPTION = "Alert"
    const val INTENT_KEY_STREAM_ID = "INTENT_KEY_STREAM_ID"
}
