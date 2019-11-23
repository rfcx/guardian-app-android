package org.rfcx.ranger.data.local

import io.realm.Realm
import org.rfcx.ranger.entity.guardian.Guardian

class GuardianDb(val realm: Realm) {
	
	fun getGuardians(): List<Guardian>? {
		return realm.copyFromRealm(realm.where(Guardian::class.java).findAllAsync())
	}
	
	fun saveGuardians(guardians: List<Guardian>) {
		Realm.getDefaultInstance().use { it ->
			it.executeTransaction {
				it.delete(Guardian::class.java)
				it.insertOrUpdate(guardians)
			}
		}
	}
}