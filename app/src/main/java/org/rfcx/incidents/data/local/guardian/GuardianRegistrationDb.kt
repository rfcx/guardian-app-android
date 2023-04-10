package org.rfcx.incidents.data.local.guardian

import io.realm.Realm
import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.entity.guardian.registration.GuardianRegistration

class GuardianRegistrationDb(private val realm: Realm) {

    fun save(registration: GuardianRegistration) {
        realm.executeTransaction {
            it.insertOrUpdate(registration)
        }
    }
}
