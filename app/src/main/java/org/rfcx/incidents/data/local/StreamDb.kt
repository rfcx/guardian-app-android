package org.rfcx.incidents.data.local

import io.realm.Realm
import org.rfcx.incidents.data.remote.streams.StreamResponse
import org.rfcx.incidents.data.remote.streams.toStream
import org.rfcx.incidents.entity.stream.Stream

class StreamDb(private val realm: Realm) {

    fun insertOrUpdate(response: StreamResponse) {
        realm.executeTransaction {
            it.insertOrUpdate(response.toStream())
        }
    }

    fun get(id: String): Stream? =
        realm.where(Stream::class.java).equalTo(Stream.STREAM_ID, id).findFirst()

    fun getByProject(projectId: String): List<Stream> =
        realm.where(Stream::class.java).equalTo(Stream.STREAM_PROJECT_ID, projectId).findAll()
}
