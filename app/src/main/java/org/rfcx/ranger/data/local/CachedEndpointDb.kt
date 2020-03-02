package org.rfcx.ranger.data.local

import io.realm.Realm
import org.rfcx.ranger.entity.CachedEndpoint
import java.util.*

class CachedEndpointDb(val realm: Realm) {
	
	fun updateCachedEndpoint(endpoint: String) {
		realm.executeTransaction {
			it.copyToRealmOrUpdate(CachedEndpoint(endpoint, Date()))
		}
	}
	
	fun hasCachedEndpoint(endpoint: String, hours: Double = 1.0): Boolean {
		val cachedEndpoint = realm.where(CachedEndpoint::class.java).equalTo(
				CachedEndpoint.FIELD_ENDPOINT, endpoint).findFirst() ?: return false
		return cachedEndpoint.updatedAt.after(Date(System.currentTimeMillis() - (hours * 60 * 60 * 1000).toInt()))
	}
}