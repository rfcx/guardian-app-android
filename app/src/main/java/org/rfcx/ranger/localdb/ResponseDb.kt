package org.rfcx.ranger.localdb

import io.realm.Realm
import io.realm.RealmResults
import org.rfcx.ranger.entity.response.Response
import org.rfcx.ranger.entity.response.SyncState

class ResponseDb(val realm: Realm) {
	fun unsentCount(): Long {
		return realm.where(Response::class.java)
				.notEqualTo(Response.RESPONSE_SYNC_STATE, SyncState.SENT.value)
				.count()
	}
	
	fun unlockSending() {
		realm.executeTransaction {
			val snapshot = it.where(Response::class.java)
					.equalTo(Response.RESPONSE_SYNC_STATE, SyncState.SENDING.value).findAll()
					.createSnapshot()
			snapshot.forEach { res ->
				res.syncState = SyncState.UNSENT.value
			}
		}
	}
	
	fun getResponseById(id: Int): Response? {
		val response = realm.where(Response::class.java).equalTo(Response.RESPONSE_ID, id).findFirst()
				?: return null
		return realm.copyFromRealm(response)
	}
	
	fun save(response: Response): Response {
		var res = response
		realm.executeTransaction {
			if (response.id == 0) {
				response.id = (it.where(Response::class.java).max("id")?.toInt() ?: 0) + 1
				res = response
			}
			it.insertOrUpdate(response)
		}
		return res
	}
	
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
	
	fun markSent(id: Int, guid: String?, incidentRef: String?) {
		mark(id, guid, SyncState.SENT.value, incidentRef)
	}
	
	private fun mark(id: Int, guid: String? = null, syncState: Int, incidentRef: String? = null) {
		realm.executeTransaction {
			val response = it.where(Response::class.java).equalTo(Response.RESPONSE_ID, id).findFirst()
			if (response != null) {
				response.guid = guid
				response.syncState = syncState
				response.incidentRef = incidentRef
			}
		}
	}
	
	fun getAllResultsAsync(): RealmResults<Response> {
		return realm.where(Response::class.java).findAllAsync()
	}
}
