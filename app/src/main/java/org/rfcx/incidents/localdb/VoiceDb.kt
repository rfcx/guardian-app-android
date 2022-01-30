package org.rfcx.incidents.localdb

import io.realm.Realm
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.entity.response.Voice

class VoiceDb(val realm: Realm) {
    fun lockUnsent(): List<Voice> {
        var unsentCopied: List<Voice> = listOf()
        realm.executeTransaction { it ->
            val unsent = it.where(Voice::class.java)
                .equalTo(Voice.FIELD_SYNC_STATE, SyncState.UNSENT.value)
                .isNotNull(Voice.FIELD_RESPONSE_SERVER_ID)
                .findAll().createSnapshot()
            unsentCopied = unsent.toList()
            unsent.forEach {
                it.syncState = SyncState.SENDING.value
            }
        }
        return unsentCopied
    }
    
    fun saveReportServerId(serverId: String, reportId: Int) {
        val voices = realm.where(Voice::class.java)
            .equalTo(Voice.FIELD_RESPONSE_ID, reportId)
            .findAll()
        realm.executeTransaction { transaction ->
            voices?.forEach {
                val voice = it.apply {
                    this.responseServerId = serverId
                }
                transaction.insertOrUpdate(voice)
            }
        }
    }
    
    fun markSent(id: Int, remotePath: String?) {
        mark(id = id, syncState = SyncState.SENT.value, remotePath)
    }
    
    fun markUnsent(id: Int) {
        mark(id = id, syncState = SyncState.UNSENT.value, null)
    }
    
    private fun mark(id: Int, syncState: Int, remotePath: String?) {
        realm.executeTransaction {
            val report = it.where(Voice::class.java).equalTo(Voice.FIELD_ID, id).findFirst()
            if (report != null) {
                report.syncState = syncState
                report.remotePath = remotePath
            }
        }
    }
    
    fun unsentCount(): Long {
        return realm.where(Voice::class.java).notEqualTo(Voice.FIELD_SYNC_STATE, SyncState.SENT.value).count()
    }
    
    fun save(response: Response) {
        realm.executeTransaction {
            response.audioLocation?.let { localPath ->
                val voiceId = (it.where(Voice::class.java).max("id")?.toInt() ?: 0) + 1
                val voice = Voice(voiceId, responseId = response.id, localPath = localPath)
                it.insertOrUpdate(voice)
            }
        }
    }
}
