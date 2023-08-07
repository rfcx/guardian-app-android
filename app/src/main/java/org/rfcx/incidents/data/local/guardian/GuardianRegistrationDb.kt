package org.rfcx.incidents.data.local.guardian

import io.realm.Realm
import io.realm.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.entity.guardian.registration.GuardianRegistration
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.entity.stream.Stream

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

    fun getAllUnsentForWorker(): List<GuardianRegistration> {
        var unsent: List<GuardianRegistration> = listOf()
        realm.executeTransaction {
            val registrations = it.where(GuardianRegistration::class.java)
                .notEqualTo(GuardianRegistration.FIELD_SYNC_STATE, SyncState.SENT.value)
                .findAll().createSnapshot()
            unsent = registrations
        }
        return unsent
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

    fun unsentCount(): Long {
        return realm.where(GuardianRegistration::class.java).notEqualTo(GuardianRegistration.FIELD_SYNC_STATE, SyncState.SENT.value).count()
    }

    fun unlockSending() {
        realm.executeTransaction { it ->
            val snapshot =
                it.where(GuardianRegistration::class.java).equalTo(GuardianRegistration.FIELD_SYNC_STATE, SyncState.SENDING.value).findAll().createSnapshot()
            snapshot.forEach {
                it.syncState = SyncState.UNSENT.value
            }
        }
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
