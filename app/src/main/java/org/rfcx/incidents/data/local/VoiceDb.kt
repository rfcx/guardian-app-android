package org.rfcx.incidents.data.local

import io.realm.Realm
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.entity.response.VoiceAsset

class VoiceDb(val realm: Realm) {
    fun lockUnsent(): List<VoiceAsset> {
        var unsentCopied: List<VoiceAsset> = listOf()
        realm.executeTransaction { it ->
            val unsent = it.where(VoiceAsset::class.java)
                .equalTo(VoiceAsset.FIELD_SYNC_STATE, SyncState.UNSENT.value)
                .isNotNull(VoiceAsset.FIELD_RESPONSE_SERVER_ID)
                .findAll().createSnapshot()
            unsentCopied = unsent.toList()
            unsent.forEach {
                it.syncState = SyncState.SENDING.value
            }
        }
        return unsentCopied
    }

    fun saveReportServerId(serverId: String, reportId: Int) {
        val voices = realm.where(VoiceAsset::class.java)
            .equalTo(VoiceAsset.FIELD_RESPONSE_ID, reportId)
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
            val report = it.where(VoiceAsset::class.java).equalTo(VoiceAsset.FIELD_ID, id).findFirst()
            if (report != null) {
                report.syncState = syncState
                report.remotePath = remotePath
            }
        }
    }

    fun unsentCount(): Long {
        return realm.where(VoiceAsset::class.java).notEqualTo(VoiceAsset.FIELD_SYNC_STATE, SyncState.SENT.value).count()
    }

    fun save(response: Response) {
        realm.executeTransaction {
            response.audioLocation?.let { localPath ->
                val voiceId = (it.where(VoiceAsset::class.java).max("id")?.toInt() ?: 0) + 1
                val voice = VoiceAsset(voiceId, responseId = response.id, localPath = localPath)
                it.insertOrUpdate(voice)
            }
        }
    }
}
