package android.rfcx.org.ranger.service

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.util.NotificationHelper
import android.rfcx.org.ranger.view.MessageListActivity
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.NotificationCompat
import android.util.Log
import android.widget.RemoteViews


/**
 * Created by Jingjoeh on 10/7/2017 AD.
 */
class LocationNotificationManager : BroadcastReceiver {
    private val notificationLocationChanelId = "Use Location"
    private val notificationLocationId = 1111
    private val actionCloseNotification = "actionCloseNotification"

    private var mMylocationService: SendLocationLocationService? = null
    private var notificationManager: NotificationManagerCompat? = null

    constructor(mylocationService: SendLocationLocationService) : super() {
        this.mMylocationService = mylocationService
        notificationManager = NotificationManagerCompat.from(mylocationService)
        notificationManager?.cancelAll()
    }


    override fun onReceive(p0: Context?, intent: Intent?) {

        val action: String? = intent?.action
        Log.d("onReceive", action)
        if (!action.isNullOrEmpty()) {
            when (action) {
                actionCloseNotification -> {
                    stopLocationNotification()
                    Log.d("onReceive", "Stop")
                }
            }
        }
    }


    fun startLocationNotification() {
        val notification = getNotification()
        val filter = IntentFilter()
        filter.addAction(actionCloseNotification)
        mMylocationService?.let {
            it.registerReceiver(this@LocationNotificationManager, filter)
            it.startForeground(notificationLocationId, notification)
        }

    }

    fun stopLocationNotification() {
        mMylocationService?.unregisterReceiver(this)
        notificationManager?.cancel(notificationLocationId)
        mMylocationService?.stopForeground(true)
        mMylocationService?.stopSelf()
    }

    fun startReLoginNotification(context: Context) {
        NotificationHelper.getInstance().showLoginNotification(context)
    }

    private fun getNotification(): Notification {

        val openAppIntent = Intent(mMylocationService, MessageListActivity::class.java)

        openAppIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val openAppPendingIntent = PendingIntent.getActivity(mMylocationService, 0,
                openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val closeServicePendingIntent = PendingIntent.getBroadcast(mMylocationService, 0,
                Intent(actionCloseNotification).setPackage(mMylocationService?.packageName), PendingIntent.FLAG_UPDATE_CURRENT)

        val smallContentView = RemoteViews(mMylocationService?.packageName, R.layout.notification_location)
        smallContentView.setTextViewText(R.id.notification_title_text, mMylocationService!!.getString(R.string.app_name))
        smallContentView.setTextViewText(R.id.notification_additional_text, mMylocationService!!.getString(R.string.user_location))
        smallContentView.setOnClickPendingIntent(R.id.notification_close_button, closeServicePendingIntent)

        val builder = NotificationCompat.Builder(mMylocationService)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(mMylocationService!!.getString(R.string.app_name))
                .setContentText(mMylocationService!!.getString(R.string.user_location))
                .setContent(smallContentView)
                .setChannelId(notificationLocationChanelId)

        builder.setContentIntent(openAppPendingIntent)

        return builder.build()
    }



}