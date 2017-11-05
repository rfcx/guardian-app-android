package android.rfcx.org.ranger.util

import android.rfcx.org.ranger.entity.event.Event
import android.rfcx.org.ranger.entity.message.Message
import io.realm.Realm

/**
 * Created by Jingjoeh on 11/5/2017 AD.
 */
class RealmHelper {

    companion object {
        @Volatile private var INSTANCE: RealmHelper? = null
        fun getInstance(): RealmHelper =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: RealmHelper().also { INSTANCE = it }
                }
    }

    fun updateOpenedMessage(message: Message) {
        message.isOpened = true
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.insertOrUpdate(message)
        realm.commitTransaction()
        realm.close()
    }

    fun updateOpenedEvent(event: Event) {
        event.isOpened = true
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.insertOrUpdate(event)
        realm.commitTransaction()
        realm.close()
    }

    fun updateConfirmedEvent(event: Event) {
        event.isConfirmed = true
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.insertOrUpdate(event)
        realm.commitTransaction()
        realm.close()
    }

    fun isOenedMessage(message: Message): Boolean {
        val realm = Realm.getDefaultInstance()
        val realmMessage: Message? = realm.where(Message::class.java)
                .equalTo(Message.messageGUID, message.guid).findFirst()
        realmMessage?.let {
            return realmMessage.isOpened
        }

        return false
    }

    fun isOenedEvent(event: Event): Boolean {
        val realm = Realm.getDefaultInstance()
        val realmEvent: Event? = realm.where(Event::class.java)
                .equalTo(Event.eventGUID, event.event_guid).findFirst()
        realmEvent?.let {
            return realmEvent.isOpened
        }
        return false
    }

    fun isConfirmedEvent(event: Event): Boolean {
        val realm = Realm.getDefaultInstance()
        val realmEvent: Event? = realm.where(Event::class.java)
                .equalTo(Event.eventGUID, event.event_guid).findFirst()
        realmEvent?.let {
            return realmEvent.isConfirmed
        }
        return false
    }
}