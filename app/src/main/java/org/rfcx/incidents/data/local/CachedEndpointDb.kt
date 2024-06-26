package org.rfcx.incidents.data.local

import io.realm.Realm
import org.rfcx.incidents.entity.common.CachedEndpoint
import java.util.Date

class CachedEndpointDb(val realm: Realm) {

    fun updateCachedEndpoint(endpoint: String) {
        realm.executeTransaction {
            it.insertOrUpdate(CachedEndpoint(endpoint, Date()))
        }
    }

    fun hasCachedEndpoint(endpoint: String, hours: Double = 1.0): Boolean {
        val cachedEndpoint = realm.where(CachedEndpoint::class.java).equalTo(
            CachedEndpoint.FIELD_ENDPOINT, endpoint
        ).findFirst() ?: return false
        return cachedEndpoint.updatedAt.after(Date(System.currentTimeMillis() - (hours * 60 * 60 * 1000).toInt()))
    }
}
