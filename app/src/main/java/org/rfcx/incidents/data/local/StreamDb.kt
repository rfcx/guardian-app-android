package org.rfcx.incidents.data.local

import io.realm.Realm
import org.rfcx.incidents.data.remote.streams.StreamResponse
import org.rfcx.incidents.data.remote.streams.toStream
import org.rfcx.incidents.entity.stream.Incident
import org.rfcx.incidents.entity.stream.Stream

class StreamDb(private val realm: Realm) {

    fun insertOrUpdate(response: StreamResponse) {
        val stream = response.toStream()
        stream.lastIncident?.let {
            stream.lastIncident = realm.where(Incident::class.java).equalTo(Incident.FIELD_ID, it.id).findFirst()
        }
        realm.executeTransaction {
            it.insertOrUpdate(stream)
        }
    }

    fun get(id: String): Stream? =
        realm.where(Stream::class.java).equalTo(Stream.STREAM_ID, id).findFirst()

    fun getByProject(projectId: String): List<Stream> =
        realm.where(Stream::class.java).equalTo(Stream.STREAM_PROJECT_ID, projectId).findAll()
}
