package org.rfcx.ranger.localdb

import android.util.Log
import io.realm.Realm
import io.realm.RealmResults
import org.rfcx.ranger.entity.guardian.GuardianGroup
import org.rfcx.ranger.entity.guardian.Site

/**
 * Data access for sites and guardian groups in the local realm db
 */

class SiteGuardianDb(val realm: Realm = Realm.getDefaultInstance()) {

    fun site(id: String): Site? {
        return realm.where(Site::class.java).equalTo("id", id).findFirst()
    }

    fun sites(): List<Site> {
        return realm.where(Site::class.java).findAll()
    }

    fun saveSites(sites: List<Site>) {
        realm.beginTransaction()

        val toBeDeleted = realm.where(Site::class.java).not().`in`("id", sites.map({ it.id }).toTypedArray()).findAll()
        Log.d("SiteGuardianDb", "saveSites: deleting ${toBeDeleted.size}")
        toBeDeleted.deleteAllFromRealm()

        Log.d("SiteGuardianDb", "saveSites: updating ${sites.size}")
        realm.insertOrUpdate(sites)

        realm.commitTransaction()
    }

    fun guardianGroup(id: String): GuardianGroup? {
        return realm.where(GuardianGroup::class.java).equalTo("id", id).findFirst()
    }

    fun guardianGroups(): RealmResults<GuardianGroup> {
        return realm.where(GuardianGroup::class.java).findAll()
    }

    fun saveGuardianGroups(groups: List<GuardianGroup>) {
        realm.beginTransaction()
        realm.insertOrUpdate(groups)
        realm.commitTransaction()
        realm.close()
    }

}