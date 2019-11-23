package org.rfcx.ranger.data.local

import io.realm.Realm
import org.rfcx.ranger.entity.CachedEndpoint
import java.util.*

class CachedEndpointDb(val realm: Realm) {
	
	fun updateCachedEndpoint(endpoint: String) {
		realm.use { it ->
			it.executeTransaction {
				it.copyToRealmOrUpdate(CachedEndpoint(endpoint, Date()))
			}
		}
	}
	
	fun clearCachedEndpoint(endpoint: String) {
		realm.where(CachedEndpoint::class.java).like(CachedEndpoint.FIELD_ENDPOINT,
				"$endpoint*").findAll().deleteAllFromRealm()
	}
	
	fun hasCachedEndpoint(endpoint: String, hours: Int = 1): Boolean {
		val cachedEndpoint = realm.where(CachedEndpoint::class.java).equalTo(
				CachedEndpoint.FIELD_ENDPOINT, endpoint).findFirst() ?: return false
		return cachedEndpoint.updatedAt.after(Date(System.currentTimeMillis() - hours * 60 * 60 * 1000))
	}
}