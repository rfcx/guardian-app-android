package android.rfcx.org.ranger.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.rfcx.org.ranger.BuildConfig
import android.rfcx.org.ranger.R
import android.rfcx.org.ranger.entity.event.Event
import android.rfcx.org.ranger.entity.EventResponse
import android.rfcx.org.ranger.entity.message.Message
import android.rfcx.org.ranger.repo.TokenExpireException
import android.rfcx.org.ranger.repo.api.EventsApi
import android.rfcx.org.ranger.repo.api.MessageApi
import android.rfcx.org.ranger.util.NotificationHelper
import android.rfcx.org.ranger.util.RemoteConfigKey
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import io.realm.Realm
import io.realm.RealmResults

/**
 * Created by Jingjoeh on 10/21/2017 AD.
 *
 * this class is pulling message and event Api and show notification for them.
 */
class PullingAlertMessageReceiver : BroadcastReceiver() {
    private val TAG = PullingAlertMessageReceiver::class.java.simpleName
    override fun onReceive(context: Context?, intent: Intent?) {

        val rangerRemote = FirebaseRemoteConfig.getInstance()
        // config for debug
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()

        rangerRemote.setConfigSettings(configSettings)
        rangerRemote.setDefaults(R.xml.ranger_remote_config_defualt)

        // cache config
        var cacheExpiration: Long = 3600 // 1 hour
        if (rangerRemote.info.configSettings.isDeveloperModeEnabled) {
            cacheExpiration = 0
        }
        rangerRemote.fetch(cacheExpiration).addOnCompleteListener {
            rangerRemote.activateFetched()
        }

        // check config on firebase , pulling event
        if (rangerRemote.getBoolean(RemoteConfigKey.REMOTE_ENABLE_NOTI_MESSAGE))
            pullingMessage(context)

        if (rangerRemote.getBoolean(RemoteConfigKey.REMOTE_ENABLE_NOTI_EVENT_ALERT))
            pullingEvent(context)


    }

    private fun pullingMessage(context: Context?) {
        if (context == null) return
        MessageApi().getMessage(context, object : MessageApi.OnMessageCallBack {
            override fun onFailed(t: Throwable?, message: String?) {
                if (t != null && t is TokenExpireException) {
                    NotificationHelper.getInstance().showLoginNotification(context)
                }

            }

            override fun onSuccess(messages: List<Message>) {
                Log.d(TAG, messages.size.toString())
                val realm = Realm.getDefaultInstance()
                realm.beginTransaction()
                val oldMessages: RealmResults<Message> = realm.where(Message::class.java).findAll()
                val oldMessageId: ArrayList<String> = ArrayList()
                oldMessages.mapTo(oldMessageId) { it.guid }
                if (oldMessages.size != messages.size) {
                    val result: List<Message> = messages.filter { it -> it.guid !in oldMessageId }
                    for (message in result) {
                        NotificationHelper.getInstance().showMessageNotification(context, message)
                    }
                }

                realm.insertOrUpdate(messages)
                realm.commitTransaction()
                realm.close()
            }

        })
    }

    private fun pullingEvent(context: Context?) {
        if (context == null) return
        EventsApi().getEvents(context, 10, object : EventsApi.OnEventsCallBack {
            override fun onFailed(t: Throwable?, message: String?) {
                if (t != null && t is TokenExpireException) {
                    NotificationHelper.getInstance().showLoginNotification(context)
                }
            }

            override fun onSuccess(event: EventResponse) {
                Log.d(TAG, event.events?.size.toString())
                val events = event.events
                if (events == null || events.isEmpty())
                    return
                val realm = Realm.getDefaultInstance()
                realm.beginTransaction()
                val oldEvent: RealmResults<Event> = realm.where(Event::class.java).findAll()
                val oldEventId: ArrayList<String> = ArrayList()
                oldEvent.mapTo(oldEventId) { it.eventGUID }

                val result: List<Event> = events.filter { it -> it.eventGUID !in oldEventId }
                for (alert in result) {
                    NotificationHelper.getInstance().showAlertNotification(context, alert)
                }

                realm.insertOrUpdate(events)
                realm.commitTransaction()
                realm.close()
            }

        })
    }
}