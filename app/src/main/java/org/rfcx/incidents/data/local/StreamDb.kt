package org.rfcx.incidents.data.local

import io.realm.Realm
import io.realm.kotlin.deleteFromRealm
import org.rfcx.incidents.entity.stream.Incident
import org.rfcx.incidents.entity.stream.Stream

class StreamDb(private val realm: Realm) {

    fun insertOrUpdate(stream: Stream) {
        stream.lastIncident?.let {
            val existingIncident = realm.where(Incident::class.java).equalTo(Incident.FIELD_ID, it.id).findFirst() ?: return@let
            stream.lastIncident = existingIncident
        }
        realm.executeTransaction {
            it.insertOrUpdate(stream)
        }
    }

    fun get(id: String): Stream? =
        realm.where(Stream::class.java).equalTo(Stream.FIELD_ID, id).findFirst()

    fun getByProject(projectId: String): List<Stream> =
        realm.where(Stream::class.java).equalTo(Stream.FIELD_PROJECT_ID, projectId).sort(Stream.FIELD_ORDER).findAll()

    fun deleteStreamsByProjectId(id: String) {
        realm.executeTransaction {
            val streams = it.where(Stream::class.java).equalTo(Stream.FIELD_PROJECT_ID, id).findAll()
            streams?.forEach { s ->
                s.deleteFromRealm()
            }
        }
    }
}
