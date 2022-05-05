package org.rfcx.incidents.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import androidx.core.app.NotificationCompat
import org.rfcx.incidents.R
import org.rfcx.incidents.service.EventNotification.INTENT_KEY_STREAM_ID
import org.rfcx.incidents.view.MainActivity

class NotificationDemo() {
    fun startDemo(context: Context) {
        Handler().postDelayed({
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(INTENT_KEY_STREAM_ID, "xqcth5uvwomx")
            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addParentStack(MainActivity::class.java)
            stackBuilder.addNextIntent(intent)
            val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            var contentText = "Chainsaw ${context.getString(R.string.detected_at)} Bear Hut #2"
            // if (event?.id != null) {
            //     contentText = "Cat ${context.getString(R.string.detected_at)} // Todo: add name "
            // }
            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(alarmSound)
                .setContentTitle("Rainforest Connection")
                .setContentText(contentText)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notification)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
            notificationManager.notify(1000, notification)
        }, 20000)
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "Ranger Alert"
        const val NOTIFICATION_CHANNEL_NAME = "Alert"
        const val NOTIFICATION_CHANNEL_DESCRIPTION = "Alert"
        const val ALERT_ID_NOTI_INTENT = "ALERT_ID_NOTI_INTENT"
    }
}
