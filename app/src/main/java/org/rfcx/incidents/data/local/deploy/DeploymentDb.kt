package org.rfcx.incidents.data.local.deploy

import io.realm.Realm
import org.rfcx.incidents.entity.guardian.deployment.Deployment
import org.rfcx.incidents.entity.response.Asset
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

    fun get(): List<Deployment> {
        val deployments = realm.where(Deployment::class.java).findAll()
        return realm.copyFromRealm(deployments)
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
}
