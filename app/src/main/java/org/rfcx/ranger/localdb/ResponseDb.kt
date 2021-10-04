package org.rfcx.ranger.localdb

import io.realm.Realm
import io.realm.RealmResults
import org.rfcx.ranger.entity.response.Response
import org.rfcx.ranger.entity.response.SyncState

class ResponseDb(val realm: Realm) {
	fun save(response: Response) {
		realm.executeTransaction {
			if (response.id == 0) {
				response.id = (it.where(Response::class.java).max("id")?.toInt() ?: 0) + 1
			}
			it.insertOrUpdate(response)
		}
	}
	
	fun unsentCount(): Long = realm.where(Response::class.java).notEqualTo("syncState", ReportDb.SENT).count()
	
	fun lockUnsent(): List<Response> {
		var unsentCopied: List<Response> = listOf()
		realm.executeTransaction {
			val unsent = it.where(Response::class.java).equalTo(Response.RESPONSE_SYNC_STATE, SyncState.UNSENT.value).isNotNull(Response.RESPONSE_SUBMITTED_AT).findAll().createSnapshot()
			unsentCopied = unsent.toList()
			unsent.forEach { response ->
				response.syncState = ReportDb.SENDING
			}
		}
		return unsentCopied
	}
	
	fun markUnsent(id: Int) {
		mark(id = id, syncState = SyncState.UNSENT.value)
	}
	
	fun markSent(id: Int, guid: String?) {
		mark(id, guid, SyncState.SENT.value)
		
	}
	
	private fun mark(id: Int, guid: String? = null, syncState: Int) {
		realm.executeTransaction {
			val response = it.where(Response::class.java).equalTo(Response.RESPONSE_ID, id).findFirst()
			if (response != null) {
				response.guid = guid
				response.syncState = syncState
			}
		}
	}
	
	fun getAllResultsAsync(): RealmResults<Response> {
		return realm.where(Response::class.java).findAllAsync()
	}
}
