package org.rfcx.ranger.localdb

import io.realm.Realm
import org.rfcx.ranger.entity.response.Response
import org.rfcx.ranger.entity.response.SyncState
import org.rfcx.ranger.entity.response.Voice

class VoiceDb(val realm: Realm) {
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
