package org.rfcx.incidents.data.local

import io.realm.Realm
import io.realm.kotlin.deleteFromRealm
import io.realm.kotlin.freeze
import io.realm.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import org.rfcx.incidents.entity.guardian.deployment.Deployment
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
            if (stream.externalId != null) {
                val existingStream = realm.where(Stream::class.java)
                    .equalTo(Stream.FIELD_EXTERNAL_ID, stream.externalId)
                    .findFirst()
                if (existingStream == null) {
                    val id = (realm.where(Stream::class.java).max(Stream.FIELD_ID)?.toInt() ?: 0) + 1
                    stream.id = id
                    it.insert(stream)
                } else {
                    stream.id = existingStream.id
                    it.insertOrUpdate(stream)
                }
            } else {
                if (stream.id == -1) {
                    val id = (realm.where(Stream::class.java).max(Stream.FIELD_ID)?.toInt() ?: 0) + 1
                    stream.id = id
                    it.insert(stream)
                } else {
                    val existingStream = realm.where(Stream::class.java)
                        .equalTo(Stream.FIELD_ID, stream.id)
                        .findFirst()
                    stream.id = existingStream!!.id
                    it.insertOrUpdate(stream)
                }
            }
        }
    }

    fun getAllAsFlow(): Flow<List<Stream>> {
        return realm.where(Stream::class.java).findAllAsync().toFlow()
    }

    fun getAllForWorker(): List<Stream> {
        var unsent: List<Stream> = listOf()
        realm.executeTransaction {
            val streams = realm.where(Stream::class.java)
                .findAll().createSnapshot()
            unsent = streams
        }
        return unsent
    }

    fun updateSiteServerId(stream: Stream, externalId: String) {
        realm.executeTransaction {
            stream.externalId = externalId
            realm.insertOrUpdate(stream)
        }
    }

    fun get(id: Int): Stream? =
        realm.where(Stream::class.java).equalTo(Stream.FIELD_ID, id).findFirst()

    fun get(id: String): Stream? =
        realm.where(Stream::class.java).equalTo(Stream.FIELD_EXTERNAL_ID, id).findFirst()

    fun getByProject(projectId: String): List<Stream> {
        val streams = realm.where(Stream::class.java).equalTo(Stream.FIELD_PROJECT_ID, projectId).sort(Stream.FIELD_ORDER).findAll()
        return realm.copyFromRealm(streams)
    }

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
