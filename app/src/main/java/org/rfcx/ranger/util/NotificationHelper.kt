package org.rfcx.ranger.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.message.Message
import org.rfcx.ranger.view.LoginActivity
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import org.rfcx.ranger.view.MessageListActivity
import java.util.*

class NotificationHelper {
    private val notificationDefaultChanelId = "Default"
    private val notificationMessageChanelId = "Message"
    private val notificationAlertChanelId = "Alert"
    private val notificationReLoginId = 1112

    companion object {
        @Volatile private var INSTANCE: NotificationHelper? = null
        fun getInstance(): NotificationHelper =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: NotificationHelper()
                }
    }

    fun showMessageNotification(context: Context, message: Message) {
        val intent = Intent(context, MessageListActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_ONE_SHOT)

        val builder = notificationBuilderWithDefaults(context)
                .setSmallIcon(R.drawable.ic_text_message)
                .setContentTitle(message.from?.firstname)
                .setContentText(message.text)
                .setChannelId(notificationMessageChanelId)
                .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager?.notify(Random().nextInt(10), builder.build())
    }

    fun showAlertNotification(context: Context, event: Event) {

      /*  val intent = Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?q="
                        + message.coords?.lat + ","
                        + message.coords?.lon))
                        */
        val intent = Intent(context, MessageListActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_ONE_SHOT)

        val builder = notificationBuilderWithDefaults(context)
                .setSmallIcon(R.drawable.ic_alert_noti)
                .setContentTitle(event.value)
                .setContentText(event.guardianShortname + " " + DateHelper.getEventTime(event))
                .setChannelId(notificationAlertChanelId)
                .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager?.notify(Random().nextInt(10), builder.build())
    }

    fun showLoginNotification(context: Context) {
        val openAppIntent = Intent(context, LoginActivity::class.java)
        openAppIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val openLoginPagePendingIntent = PendingIntent.getActivity(context, 0,
                openAppIntent, PendingIntent.FLAG_ONE_SHOT)

        val builder = notificationBuilderWithDefaults(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.re_login))
                .setChannelId(notificationDefaultChanelId)
                .setContentIntent(openLoginPagePendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager?.notify(notificationReLoginId, builder.build())
    }

    private fun notificationBuilderWithDefaults(context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(longArrayOf(1000, 1000))
                .setAutoCancel(true)
    }
}