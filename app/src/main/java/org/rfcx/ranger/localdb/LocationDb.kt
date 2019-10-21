package org.rfcx.ranger.localdb

import io.realm.Realm
import io.realm.RealmResults
import org.rfcx.ranger.entity.location.CheckIn

/**
 * Data access for location data in the local realm db
 */

class LocationDb(val realm: Realm) {

    init {
        realm.refresh()
    }

    fun save(checkin: CheckIn) {
        realm.executeTransaction {
            if (checkin.id == 0) {
                checkin.id = (it.where(CheckIn::class.java).max("id")?.toInt() ?: 0) + 1
            }
            it.insertOrUpdate(checkin)
        }
    }

    fun unsentCount(): Long {
        return realm.where(CheckIn::class.java).equalTo("synced", false).count()
    }

    fun unsent(): List<CheckIn> {
        return realm.copyFromRealm(realm.where(CheckIn::class.java).equalTo("synced", false).findAll().createSnapshot().toList())
    }

    fun markSent(ids: List<Int>) {
        realm.executeTransaction {
            it.where(CheckIn::class.java).`in`("id", ids.toTypedArray()).findAll().setBoolean("synced", true)
        }
    }

    fun allForDisplay(): RealmResults<CheckIn> {
        return realm.where(CheckIn::class.java).greaterThan("timestamp",
                System.currentTimeMillis() - (MAX_DISPLAY_AGE_HOURS * 3600000)).findAllAsync()
    }

    fun deleteSynced(): Long {
        val query = realm.where(CheckIn::class.java).equalTo("synced", false).lessThan("timestamp", System.currentTimeMillis() - (MIN_DELETION_AGE_HOURS * 3600000))
        val count = query.count()
        if (count > 0) {
            realm.executeTransaction {
                query.findAll().deleteAllFromRealm()
            }
        }
        return count
    }

    companion object {
        const val MAX_DISPLAY_AGE_HOURS = 6
        const val MIN_DELETION_AGE_HOURS = 72
    }
}