package org.rfcx.incidents.data.local.deploy

import io.realm.Realm
import io.realm.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.entity.guardian.deployment.Deployment
import org.rfcx.incidents.entity.response.SyncState

class DeploymentDb(private val realm: Realm) {

    fun insert(deployment: Deployment) {
        realm.executeTransaction {
            if (deployment.externalId != null) {
                val externalDeployment = getById(deployment.externalId!!)
                // do nothing if already have deployment in local
                if (externalDeployment == null) {
                    // create new if there is none
                    val id = (realm.where(Deployment::class.java).max(Deployment.FIELD_ID)?.toInt() ?: 0) + 1
                    deployment.id = id
                    it.insertOrUpdate(deployment)
                } else {
                    // Only update deployment that not need for syncing
                    if (externalDeployment.syncState == SyncState.SENT.value) {
                        externalDeployment.images = deployment.images
                        externalDeployment.deployedAt = deployment.deployedAt
                        externalDeployment.deviceParameters = deployment.deviceParameters
                        externalDeployment.syncState = deployment.syncState
                        externalDeployment.externalId = deployment.externalId
                        it.insertOrUpdate(externalDeployment)
                    }
                }
            } else if (deployment.id == 0) {
                val id = (realm.where(Deployment::class.java).max(Deployment.FIELD_ID)?.toInt() ?: 0) + 1
                deployment.id = id
                it.insertOrUpdate(deployment)
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
        return realm.where(Deployment::class.java).equalTo(Deployment.FIELD_DEPLOYMENT_KEY, deployment.deploymentKey).findFirst()!!
    }

    fun list(): List<Deployment> {
        val deployments = realm.where(Deployment::class.java).findAll()
        return realm.copyFromRealm(deployments)
    }

    fun getById(id: Int): Deployment? {
        val deployment = realm.where(Deployment::class.java).equalTo(Deployment.FIELD_ID, id).findFirst() ?: return null
        return realm.copyFromRealm(deployment)
    }

    fun getById(id: String): Deployment? {
        val deployment = realm.where(Deployment::class.java).equalTo(Deployment.FIELD_EXTERNAL_ID, id).findFirst() ?: return null
        return realm.copyFromRealm(deployment)
    }

    fun getByIds(id: String): List<Deployment?> {
        val deployment = realm.where(Deployment::class.java).equalTo(Deployment.FIELD_EXTERNAL_ID, id).findAll() ?: return listOf()
        return realm.copyFromRealm(deployment)
    }

    fun listForWorker(): List<Deployment> {
        var unsent: List<Deployment> = listOf()
        realm.executeTransaction {
            val deployments = realm.where(Deployment::class.java)
                .findAll().createSnapshot()
            unsent = deployments
        }
        return unsent
    }

    fun listAsFlow(): Flow<List<Deployment>> {
        return realm.where(Deployment::class.java).findAllAsync().toFlow()
    }

    fun getByIdAsFlow(id: Int): Flow<Deployment?> {
        val deployment = realm.where(Deployment::class.java).equalTo(Deployment.FIELD_ID, id)
        return deployment.findFirstAsync().toFlow()
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
                if (serverId != null) {
                    deployment.externalId = serverId
                }
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
