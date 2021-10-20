package org.rfcx.ranger.localdb

import io.realm.Realm
import org.rfcx.ranger.entity.location.Tracking
import org.rfcx.ranger.entity.location.TrackingFile
import org.rfcx.ranger.entity.response.SyncState

class TrackingFileDb(private val realm: Realm) {
	fun insertOrUpdate(file: TrackingFile) {
		realm.executeTransaction {
			if (file.id == 0) {
				val id = (realm.where(Tracking::class.java).max(Tracking.TRACKING_ID)?.toInt()
						?: 0) + 1
				file.id = id
			}
			it.insertOrUpdate(file)
		}
	}
	
	fun updateResponseServerId(responseId: Int, serverId: String?) {
		realm.executeTransaction {
			//update server id in track
			it.where(TrackingFile::class.java)
					.equalTo(TrackingFile.FIELD_RESPONSE_ID, responseId)
					.findFirst()?.apply {
						this.responseServerId = serverId
					}
		}
	}
	
	fun lockUnsent(): List<TrackingFile> {
		var unsentCopied: List<TrackingFile> = listOf()
		realm.executeTransaction {
			val unsent = it.where(TrackingFile::class.java)
					.equalTo(TrackingFile.FIELD_SYNC_STATE, SyncState.UNSENT.value)
					.isNotNull(TrackingFile.FIELD_RESPONSE_SERVER_ID)
					.findAll()
					.createSnapshot()
			unsentCopied = unsent.toList()
			unsent.forEach { d -> d.syncState = SyncState.SENT.value }
		}
		return unsentCopied
	}
	
	fun markSent(id: Int, remotePath: String?) {
		realm.executeTransaction {
			val file = it.where(TrackingFile::class.java).equalTo(TrackingFile.FIELD_ID, id).findFirst()
			if (file != null) {
				file.syncState = SyncState.SENT.value
				file.remotePath = remotePath
			}
		}
	}
	
	fun markUnsent(id: Int) {
		realm.executeTransaction {
			val file = it.where(TrackingFile::class.java).equalTo(TrackingFile.FIELD_ID, id).findFirst()
			if (file != null) {
				file.syncState = SyncState.UNSENT.value
			}
		}
	}
}
