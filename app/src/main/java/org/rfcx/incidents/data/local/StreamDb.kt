package org.rfcx.incidents.data.local

import android.util.Log
import io.realm.Realm
import io.realm.kotlin.deleteFromRealm
import io.realm.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
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
                    if (stream.latitude == 0.0) {
                        stream.latitude = existingStream.latitude
                    }
                    if (stream.longitude == 0.0) {
                        stream.longitude = existingStream.longitude
                    }
                    if (stream.altitude == 0.0) {
                        stream.altitude = existingStream.altitude
                    }
                    if (stream.timezoneRaw == "") {
                        stream.timezoneRaw = existingStream.timezoneRaw
                    }
                    if (stream.projectId == "") {
                        stream.projectId = existingStream.projectId
                    }
                    if (stream.tags.isNullOrEmpty()) {
                        stream.tags = existingStream.tags
                    }
                    if (stream.lastIncident == null) {
                        stream.lastIncident = existingStream.lastIncident
                    }
                    if (stream.guardianType == null) {
                        stream.guardianType = existingStream.guardianType
                    }
                    if (stream.order == Int.MAX_VALUE) {
                        stream.order = existingStream.order
                    }
                    if (stream.deployment == null) {
                        stream.deployment = existingStream.deployment
                    }
                    it.copyToRealmOrUpdate(stream)
                }
            } else {
                if (stream.id == -1) {
                    val id = (realm.where(Stream::class.java).max(Stream.FIELD_ID)?.toInt() ?: 0) + 1
                    stream.id = id
                    it.copyToRealmOrUpdate(stream)
                } else {
                    val existingStream = realm.where(Stream::class.java)
                        .equalTo(Stream.FIELD_ID, stream.id)
                        .findFirst()
                    stream.id = existingStream!!.id
                    it.copyToRealmOrUpdate(stream)
                }
            }
        }
    }

    fun getAllAsFlow(): Flow<List<Stream>> {
        return realm.where(Stream::class.java).findAllAsync().toFlow()
    }

    fun getAllAsFlowByProject(projectId: String): Flow<List<Stream>> {
        return realm.where(Stream::class.java).equalTo(Stream.FIELD_PROJECT_ID, projectId).findAllAsync().toFlow()
        // return flow { emit(realm.where(Stream::class.java).equalTo(Stream.FIELD_PROJECT_ID, projectId).findAll()) }
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
            realm.copyToRealmOrUpdate(stream)
        }
    }

    fun updateDeployment(stream: Stream, deployment: Deployment) {
        realm.executeTransaction {
            stream.deployment = deployment
            realm.copyToRealmOrUpdate(stream)
        }
    }

    fun get(id: Int, needCopy: Boolean = true): Stream? {
        val stream = realm.where(Stream::class.java).equalTo(Stream.FIELD_ID, id).findFirst() ?: return null
        if (needCopy) return realm.copyFromRealm(stream)
        return stream
    }

    fun getByIdAsFlow(id: Int): Flow<Stream?> {
        val stream = realm.where(Stream::class.java).equalTo(Stream.FIELD_ID, id)
        return stream.findFirstAsync().toFlow()
    }

    fun get(id: String, needCopy: Boolean = true): Stream? {
        val stream = realm.where(Stream::class.java).equalTo(Stream.FIELD_EXTERNAL_ID, id).findFirst() ?: return null
        if (needCopy) return realm.copyFromRealm(stream)
        return stream
    }

    fun getByProject(projectId: String?, needCopy: Boolean = true): List<Stream> {
        if (projectId == null) {
            val streams = realm.where(Stream::class.java).sort(Stream.FIELD_ORDER).findAll()
            if (needCopy) return realm.copyFromRealm(streams)
            return streams
        }
        val streams = realm.where(Stream::class.java).equalTo(Stream.FIELD_PROJECT_ID, projectId).sort(Stream.FIELD_ORDER).findAll()
        if (needCopy) return realm.copyFromRealm(streams)
        return streams
    }

    fun deleteByProject(projectId: String, callback: (Boolean) -> Unit) {
        realm.executeTransaction {
            val streams = it.where(Stream::class.java).equalTo(Stream.FIELD_PROJECT_ID, projectId).isNotNull(Stream.FIELD_EXTERNAL_ID).findAll()
            streams?.forEach { s ->
                s.deployment?.deleteFromRealm()
                s.deployment?.images?.deleteAllFromRealm()
                s.lastIncident?.deleteFromRealm()
                s.deleteFromRealm()
            }
            callback.invoke(true)
        }
        callback.invoke(false)
    }

    fun deleteByProject(projectId: String) {
        realm.executeTransaction {
            val streams = it.where(Stream::class.java).equalTo(Stream.FIELD_PROJECT_ID, projectId).isNotNull(Stream.FIELD_EXTERNAL_ID).findAll()
            streams?.forEach { s ->
                s.deployment?.deleteFromRealm()
                s.deployment?.images?.deleteAllFromRealm()
                s.lastIncident?.deleteFromRealm()
                s.deleteFromRealm()
            }
        }
    }
}
