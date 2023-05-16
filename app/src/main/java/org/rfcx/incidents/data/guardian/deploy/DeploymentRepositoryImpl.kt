package org.rfcx.incidents.data.guardian.deploy

import org.rfcx.incidents.data.interfaces.guardian.deploy.DeploymentRepository
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.local.deploy.DeploymentDb
import org.rfcx.incidents.data.local.deploy.DeploymentImageDb
import org.rfcx.incidents.entity.guardian.deployment.Deployment

class DeploymentRepositoryImpl(
    private val deploymentLocal: DeploymentDb,
    private val imageLocal: DeploymentImageDb,
    private val streamLocal: StreamDb
) : DeploymentRepository {

    override fun save(deployment: Deployment) {
        // Save image first before insert deployment
        deployment.images.forEach {
            imageLocal.insert(it)
        }
        streamLocal.insertOrUpdate(deployment.stream)
        deploymentLocal.insert(deployment)
    }
}
