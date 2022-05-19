package org.rfcx.incidents.data.local

import io.realm.Realm
import io.realm.kotlin.deleteFromRealm
import org.rfcx.incidents.entity.stream.Incident
import org.rfcx.incidents.entity.stream.Stream

class StreamDb(private val realm: Realm) {

    fun insertOrUpdate(stream: Stream) {
        stream.lastIncident?.let {
            realm.executeTransaction { r ->
                val existingIncident = realm.where(Incident::class.java).equalTo(Incident.FIELD_ID, it.id).findFirst()
                existingIncident?.deleteFromRealm()
                r.insertOrUpdate(it)
            }
            val existing = realm.where(Incident::class.java).equalTo(Incident.FIELD_ID, it.id).findFirst()
            stream.lastIncident = existing
        }
        realm.executeTransaction {
            it.insertOrUpdate(stream)
        }
    }

    fun get(id: String): Stream? =
        realm.where(Stream::class.java).equalTo(Stream.FIELD_ID, id).findFirst()

    fun getByProject(projectId: String): List<Stream> =
        realm.where(Stream::class.java).equalTo(Stream.FIELD_PROJECT_ID, projectId).sort(Stream.FIELD_ORDER).findAll()

    fun deleteByProject(projectId: String, callback: (Boolean) -> Unit) {
        realm.executeTransaction {
            val streams = it.where(Stream::class.java).equalTo(Stream.FIELD_PROJECT_ID, projectId).findAll()
            streams?.forEach { s ->
                s.deleteFromRealm()
            }
            callback.invoke(true)
        }
        callback.invoke(false)
    }
}
