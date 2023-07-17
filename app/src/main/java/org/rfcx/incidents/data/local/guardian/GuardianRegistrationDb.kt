package org.rfcx.incidents.data.local.guardian

import io.realm.Realm
import io.realm.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.entity.guardian.registration.GuardianRegistration
import org.rfcx.incidents.entity.response.SyncState

class GuardianRegistrationDb(private val realm: Realm) {

    fun save(registration: GuardianRegistration) {
        realm.executeTransaction {
            it.insertOrUpdate(registration)
        }
    }

    fun list(): List<GuardianRegistration> {
        return realm.where(GuardianRegistration::class.java).findAll()
    }

    fun listAsFlow(): Flow<List<GuardianRegistration>> {
        return realm.where(GuardianRegistration::class.java).findAll().toFlow()
    }

    fun markUnsent(guid: String) {
        mark(guid, SyncState.UNSENT.value)
    }

    fun markSent(guid: String) {
        mark(guid, SyncState.SENT.value)
    }

    fun markSending(guid: String) {
        mark(guid, SyncState.SENDING.value)
    }

    private fun mark(guid: String, syncState: Int) {
        realm.executeTransaction {
            val registration =
                it.where(GuardianRegistration::class.java).equalTo(GuardianRegistration.FIELD_GUID, guid)
                    .findFirst()
            if (registration != null) {
                registration.syncState = syncState
                it.insertOrUpdate(registration)
            }
        }
    }
}
