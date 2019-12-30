package org.rfcx.ranger.data.local

import io.realm.Realm
import org.rfcx.ranger.entity.guardian.GuardianGroup

class GuardianGroupDb(val realm: Realm) {
	fun getGuardianGroup(shortName: String): GuardianGroup? {
		val guardianGroup = realm.where(GuardianGroup::class.java)
				.equalTo("shortname", shortName)
				.findFirst()
		return if (guardianGroup != null) realm.copyFromRealm(guardianGroup) else null
	}
}