package org.rfcx.incidents.data.local

import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.deleteFromRealm
import org.rfcx.incidents.entity.event.Event
import org.rfcx.incidents.entity.stream.Incident

class EventDb(private val realm: Realm) {

    fun getIncident(streamId: String): Incident? {
        return realm.where(Incident::class.java).equalTo(Incident.FIELD_ID, streamId).findFirst()
    }
    fun insertOrUpdate(event: Event, attachedToIncidentId: String) {
        realm.executeTransaction {
            event.incident = realm.where(Incident::class.java).equalTo(Incident.FIELD_ID, attachedToIncidentId).findFirst()
            it.insertOrUpdate(event)
        }
    }

    fun insertOrUpdate(event: Event) {
        realm.executeTransaction {
            it.insertOrUpdate(event)
        }
    }

    fun getEventCount(streamId: String): Long =
        realm.where(Event::class.java).equalTo(Event.EVENT_STREAM_ID, streamId).count()

    fun getEvents(streamId: String): List<Event> =
        realm.where(Event::class.java).equalTo(Event.EVENT_STREAM_ID, streamId).sort(Event.EVENT_START, Sort.ASCENDING)
            .findAll()

    fun getEvent(id: String): Event? =
        realm.where(Event::class.java).equalTo(Event.EVENT_ID, id).findFirst()

    fun getEventsByDescending(streamId: String): List<Event> =
        realm.where(Event::class.java).equalTo(Event.EVENT_STREAM_ID, streamId).sort(Event.EVENT_START, Sort.DESCENDING)
            .findAll()

    fun getEventsAsync(streamId: String, sort: Sort = Sort.DESCENDING): RealmResults<Event> {
        return realm.where(Event::class.java).equalTo(Event.EVENT_STREAM_ID, streamId).sort(Event.EVENT_START, sort).findAllAsync()
    }

    fun deleteEventsByStreamId(id: String) {
        realm.executeTransaction {
            val events = it.where(Event::class.java).equalTo(Event.EVENT_STREAM_ID, id).findAll()
            events?.forEach { a ->
                a.deleteFromRealm()
            }
        }
    }
}
