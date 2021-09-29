package org.rfcx.ranger.localdb

import io.realm.Realm
import io.realm.RealmResults
import org.rfcx.ranger.entity.response.Response

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
	
	fun getAllResultsAsync(): RealmResults<Response> {
		return realm.where(Response::class.java).findAllAsync()
	}
}
