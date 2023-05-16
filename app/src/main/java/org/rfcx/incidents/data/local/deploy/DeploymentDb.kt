package org.rfcx.incidents.data.local.deploy

import io.realm.Realm
import org.rfcx.incidents.entity.guardian.deployment.Deployment

class DeploymentDb(private val realm: Realm) {

    fun insert(deployment: Deployment) {
        realm.executeTransaction {
            val existingDeployment = realm.where(Deployment::class.java).equalTo(Deployment.FIELD_EXTERNAL_ID, deployment.externalId).findFirst()
            if (existingDeployment == null) {
                val id = (realm.where(Deployment::class.java).max(Deployment.FIELD_ID)?.toInt() ?: 0) + 1
                deployment.id = id
                it.insert(deployment)
            } else {
                deployment.id = existingDeployment.id
                it.insertOrUpdate(deployment)
            }
        }
    }
}
