package org.rfcx.ranger.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.message.Message
import org.rfcx.ranger.view.LoginActivity
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import java.util.*

/**
 * Created by Jingjoeh on 10/21/2017 AD.
 */
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
/*
        val intent = Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?q="
                        + message.coords?.lat + ","
                        + message.coords?.lon))
        val openMessagePending = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_ONE_SHOT)*/

        val builder = NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_text_message)
                .setContentTitle(message.from?.firstname)
                .setContentText(message.text)
                .setAutoCancel(true)
                .setChannelId(notificationMessageChanelId)
//                .setContentIntent(openMessagePending)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager?.notify(Random().nextInt(10), builder.build())
    }

    fun showAlertNotification(context: Context,event: Event) {

      /*  val intent = Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?q="
                        + message.coords?.lat + ","
                        + message.coords?.lon))
        val openMessagePending = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_ONE_SHOT)*/

        val builder = NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_alert_noti)
                .setContentTitle(event.value)
                .setContentText(event.site)
                .setAutoCancel(true)
                .setChannelId(notificationAlertChanelId)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager?.notify(Random().nextInt(10), builder.build())
    }

    fun showLoginNotification(context: Context) {
        val openAppIntent = Intent(context, LoginActivity::class.java)
        openAppIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val openLoginPagePendingIntent = PendingIntent.getActivity(context, 0,
                openAppIntent, PendingIntent.FLAG_ONE_SHOT)
        val builder = NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.re_login))
                .setChannelId(notificationDefaultChanelId)
                .setAutoCancel(true)
                .setContentIntent(openLoginPagePendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager?.notify(notificationReLoginId, builder.build())
    }
}