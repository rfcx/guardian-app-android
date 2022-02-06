package org.rfcx.incidents.data.local

import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.deleteFromRealm
import org.rfcx.incidents.entity.event.Event

class AlertDb(private val realm: Realm) {

    fun insertAlert(eventObj: Event) {
        realm.executeTransaction {
            it.insertOrUpdate(eventObj)
        }
    }

    fun getAlertCount(streamId: String): Long =
        realm.where(Event::class.java).equalTo(Event.ALERT_STREAM_ID, streamId).count()

    fun getAlerts(streamId: String): List<Event> =
        realm.where(Event::class.java).equalTo(Event.ALERT_STREAM_ID, streamId).sort(Event.ALERT_START, Sort.ASCENDING)
            .findAll()

    fun getAlert(id: String): Event? =
        realm.where(Event::class.java).equalTo(Event.ALERT_ID, id).findFirst()

    fun getAlertsByDescending(streamId: String): List<Event> =
        realm.where(Event::class.java).equalTo(Event.ALERT_STREAM_ID, streamId).sort(Event.ALERT_START, Sort.DESCENDING)
            .findAll()

    fun getAllResultsAsync(): RealmResults<Event> {
        return realm.where(Event::class.java).sort(Event.ALERT_START, Sort.DESCENDING).findAllAsync()
    }

    fun deleteAlertsByStreamId(id: String) {
        realm.executeTransaction {
            val events = it.where(Event::class.java).equalTo(Event.ALERT_STREAM_ID, id).findAll()
            events?.forEach { a ->
                a.deleteFromRealm()
            }
        }
    }
}
