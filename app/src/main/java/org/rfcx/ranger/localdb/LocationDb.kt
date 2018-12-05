package org.rfcx.ranger.localdb

import io.realm.Realm
import io.realm.RealmResults
import org.rfcx.ranger.entity.location.CheckIn

/**
 * TODO: Add a class description
 */

class LocationDb(val realm: Realm = Realm.getDefaultInstance()) {

    fun save(checkin: CheckIn) {
        realm.executeTransaction {
            if (checkin.id == 0) {
                checkin.id = (it.where(CheckIn::class.java).max("id")?.toInt() ?: 0) + 1
            }
            it.insertOrUpdate(checkin)
        }
    }

    fun all(maxAgeHours: Int = 6): RealmResults<CheckIn> {
        return realm.where(CheckIn::class.java).greaterThan("timestamp", System.currentTimeMillis() - (maxAgeHours * 3600000)).findAll()
    }

    fun unsentCount(): Long {
        return realm.where(CheckIn::class.java).equalTo("synced", false).count()
    }

    fun unsent(): List<CheckIn> {
        return realm.where(CheckIn::class.java).equalTo("synced", false).findAll().createSnapshot().toList()
    }

    fun markSent(ids: List<Int>) {
        realm.executeTransaction {
            it.where(CheckIn::class.java).`in`("id", ids.toTypedArray()).findAll().setBoolean("synced", true)
        }
    }

}