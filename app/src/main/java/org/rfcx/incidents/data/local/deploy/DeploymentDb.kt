package org.rfcx.incidents.data.local.deploy

import io.realm.Realm
import io.realm.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.entity.guardian.deployment.Deployment
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.entity.stream.Stream

class DeploymentDb(private val realm: Realm) {

    fun insert(deployment: Deployment) {
        realm.executeTransaction {
            if (deployment.id == 0) {
                val id = (realm.where(Deployment::class.java).max(Deployment.FIELD_ID)?.toInt() ?: 0) + 1
                deployment.id = id
                it.insert(deployment)
            } else {
                val existingDeployment = realm.where(Deployment::class.java)
                    .equalTo(Deployment.FIELD_ID, deployment.id)
                    .findFirst()
                deployment.id = existingDeployment!!.id
                it.insertOrUpdate(deployment)
            }
        }
    }

    fun insertWithResult(deployment: Deployment): Deployment {
        insert(deployment)
        return realm.where(Deployment::class.java).equalTo(Deployment.FIELD_ID, deployment.id).findFirst()!!
    }

    fun get(): List<Deployment> {
        val deployments = realm.where(Deployment::class.java).findAll()
        return realm.copyFromRealm(deployments)
    }

    fun getById(id: Int): Deployment? {
        val deployment = realm.where(Deployment::class.java).equalTo(Deployment.FIELD_ID, id).findFirst()
        return realm.copyFromRealm(deployment)
    }

    fun getById(id: String): Deployment? {
        val deployment = realm.where(Deployment::class.java).equalTo(Deployment.FIELD_DEPLOYMENT_KEY, id).findFirst()
        return realm.copyFromRealm(deployment)
    }

    fun getAllForWorker(): List<Deployment> {
        var unsent: List<Deployment> = listOf()
        realm.executeTransaction {
            val registrations = realm.where(Deployment::class.java)
                .findAll().createSnapshot()
            unsent = registrations
        }
        return unsent
    }

    fun getAsFlow(): Flow<List<Deployment>> {
        return realm.where(Deployment::class.java).findAllAsync().toFlow()
    }

    fun lockUnsent(): List<Deployment> {
        var unsentCopied: List<Deployment> = listOf()
        realm.executeTransaction {
            val unsent = it.where(Deployment::class.java)
                .equalTo(Deployment.FIELD_SYNC_STATE, SyncState.UNSENT.value)
                .findAll().createSnapshot()
            unsentCopied = unsent.toList()
            unsent.forEach {
                it.syncState = SyncState.SENDING.value
            }
        }
        return unsentCopied
    }

    fun markUnsent(id: Int) {
        mark(id = id, syncState = SyncState.UNSENT.value)
    }

    fun markSent(serverId: String, id: Int) {
        mark(id, serverId, SyncState.SENT.value)
    }

    fun markSending(id: Int) {
        mark(id = id, syncState = SyncState.SENDING.value)
    }

    private fun mark(id: Int, serverId: String? = null, syncState: Int) {
        realm.executeTransaction {
            val deployment =
                it.where(Deployment::class.java).equalTo(Deployment.FIELD_ID, id)
                    .findFirst()
            if (deployment != null) {
                deployment.externalId = serverId
                deployment.syncState = syncState
                it.insertOrUpdate(deployment)
            }
        }
    }

    fun unlockSending() {
        realm.executeTransaction { it ->
            val snapshot = it.where(Deployment::class.java).equalTo(Deployment.FIELD_SYNC_STATE, SyncState.SENDING.value).findAll().createSnapshot()
            snapshot.forEach {
                it.syncState = SyncState.UNSENT.value
            }
        }
    }

    fun unsentCount(): Long {
        return realm.where(Deployment::class.java).notEqualTo(Deployment.FIELD_SYNC_STATE, SyncState.SENT.value).count()
    }
}
