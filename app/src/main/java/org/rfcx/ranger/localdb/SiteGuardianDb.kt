package org.rfcx.ranger.localdb

import io.realm.Realm
import io.realm.RealmResults
import org.rfcx.ranger.entity.guardian.GuardianGroup
import org.rfcx.ranger.entity.guardian.Site

/**
 * Data access for sites and guardian groups in the local realm db
 */

class SiteGuardianDb(val realm: Realm) {

    fun site(id: String): Site? {
        return realm.where(Site::class.java).equalTo("id", id).findFirst()
    }

    fun sites(): List<Site> {
        return realm.where(Site::class.java).findAll()
    }

    fun saveSites(sites: List<Site>) {
        realm.beginTransaction()
        val toBeDeleted = realm.where(Site::class.java).not().`in`("id", sites.map({ it.id }).toTypedArray()).findAll()
        toBeDeleted.deleteAllFromRealm()
        realm.insertOrUpdate(sites)
        realm.commitTransaction()
    }

    fun guardianGroup(shortname: String): GuardianGroup? {
        return realm.where(GuardianGroup::class.java).equalTo("shortname", shortname).findFirst()
    }

    fun guardianGroups(): List<GuardianGroup> {
        return realm.copyFromRealm(realm.where(GuardianGroup::class.java).findAll())
    }

    fun saveGuardianGroups(groups: List<GuardianGroup>) {
        realm.beginTransaction()
        val toBeDeleted = realm.where(GuardianGroup::class.java).not()
                .`in`("shortname", groups.map({ it.shortname }).toTypedArray()).findAll()
        toBeDeleted.deleteAllFromRealm()
        realm.insertOrUpdate(groups)
        realm.commitTransaction()
        realm.refresh()
    }

}