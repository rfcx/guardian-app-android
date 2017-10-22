package android.rfcx.org.ranger.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.rfcx.org.ranger.entity.Event
import android.rfcx.org.ranger.entity.EventResponse
import android.rfcx.org.ranger.entity.message.Message
import android.rfcx.org.ranger.repo.TokenExpireException
import android.rfcx.org.ranger.repo.api.EventsApi
import android.rfcx.org.ranger.repo.api.MessageApi
import android.rfcx.org.ranger.util.NotificationHelper
import android.util.Log
import io.realm.Realm
import io.realm.RealmResults

/**
 * Created by Jingjoeh on 10/21/2017 AD.
 */
class MessageReceiver : BroadcastReceiver() {
    private val TAG = MessageReceiver::class.java.simpleName
    override fun onReceive(context: Context?, intent: Intent?) {
        MessageApi().getMessage(context!!, object : MessageApi.OnMessageCallBack {
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

                oldMessages.deleteAllFromRealm()
                realm.insert(messages)
                realm.commitTransaction()
                realm.close()
            }

        })

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

            oldEvent.deleteAllFromRealm()
            realm.insert(events)
            realm.commitTransaction()
            realm.close()
        }

    })

}
}