package org.rfcx.incidents.localdb

import io.realm.Realm
import io.realm.RealmResults
import org.rfcx.incidents.data.api.streams.StreamResponse
import org.rfcx.incidents.data.api.streams.toStream
import org.rfcx.incidents.entity.Stream

class StreamDb(private val realm: Realm) {
    fun insertStream(response: StreamResponse) {
        realm.executeTransaction {
            val stream = it.where(Stream::class.java)
                .equalTo(Stream.STREAM_SERVER_ID, response.id)
                .findFirst()
            val streamObj = response.toStream()
            if (stream == null) {
                streamObj.id = (
                    it.where(Stream::class.java).max(Stream.STREAM_ID)
                        ?.toInt() ?: 0
                    ) + 1
            } else {
                streamObj.id = stream.id
            }
            it.insertOrUpdate(streamObj)
        }
    }

    fun getAllResultsAsync(): RealmResults<Stream> {
        return realm.where(Stream::class.java).findAllAsync()
    }

    fun getStreams(): List<Stream> {
        return realm.where(Stream::class.java).findAll() ?: arrayListOf()
    }

    fun getStreamByCoreId(serverId: String): Stream? =
        realm.where(Stream::class.java).equalTo(Stream.STREAM_SERVER_ID, serverId).findFirst()

    fun getStreamsByProjectCoreId(projectCodeId: String): List<Stream> =
        realm.where(Stream::class.java).equalTo(Stream.STREAM_PROJECT_SERVER_ID, projectCodeId).findAll()
            ?: arrayListOf()

    fun getStreamByName(name: String): Stream? =
        realm.where(Stream::class.java).equalTo(Stream.STREAM_NAME, name).findFirst()

    fun saveIncidentRef(streamObj: Stream) {
        realm.executeTransaction {
            val stream = it.where(Stream::class.java)
                .equalTo(Stream.STREAM_SERVER_ID, streamObj.serverId)
                .findFirst()
            if (stream == null) {
                streamObj.id = (
                    it.where(Stream::class.java).max(Stream.STREAM_ID)
                        ?.toInt() ?: 0
                    ) + 1
            } else {
                streamObj.id = stream.id
                streamObj.incidentRef = if (streamObj.incidentRef == 0) stream.incidentRef else streamObj.incidentRef
            }
            it.insertOrUpdate(streamObj)
        }
    }
}
