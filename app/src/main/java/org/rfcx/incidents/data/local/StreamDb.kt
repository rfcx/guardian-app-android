package org.rfcx.incidents.data.local

import io.realm.Realm
import io.realm.RealmResults
import org.rfcx.incidents.data.remote.streams.StreamResponse
import org.rfcx.incidents.data.remote.streams.toStream
import org.rfcx.incidents.entity.stream.Stream

class StreamDb(private val realm: Realm) {

    fun insertOrUpdate(response: StreamResponse) {
        realm.executeTransaction {
            val existingStream = it.where(Stream::class.java)
                .equalTo(Stream.STREAM_SERVER_ID, response.id)
                .findFirst()
            val streamObj = response.toStream()
            streamObj.id = existingStream?.id ?: ((it.where(Stream::class.java).max(Stream.STREAM_ID)?.toInt() ?: 0) + 1)
            it.insertOrUpdate(streamObj)
        }
    }

    fun getAllAsync(): RealmResults<Stream> {
        return realm.where(Stream::class.java).findAllAsync()
    }

    fun getAll(): List<Stream> {
        return realm.where(Stream::class.java).findAll()
    }

    fun getStream(serverId: String): Stream? =
        realm.where(Stream::class.java).equalTo(Stream.STREAM_SERVER_ID, serverId).findFirst()

    fun getStreamsByProject(projectId: String): List<Stream> =
        realm.where(Stream::class.java).equalTo(Stream.STREAM_PROJECT_ID, projectId).findAll()
}
