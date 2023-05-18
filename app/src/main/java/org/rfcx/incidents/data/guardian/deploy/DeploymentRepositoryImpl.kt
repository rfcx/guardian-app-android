package org.rfcx.incidents.data.guardian.deploy

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.deploy.DeploymentRepository
import org.rfcx.incidents.data.local.StreamDb
import org.rfcx.incidents.data.local.deploy.DeploymentDb
import org.rfcx.incidents.data.local.deploy.DeploymentImageDb
import org.rfcx.incidents.data.remote.streams.realmList
import org.rfcx.incidents.entity.guardian.deployment.Deployment

class DeploymentRepositoryImpl(
    private val deploymentLocal: DeploymentDb,
    private val imageLocal: DeploymentImageDb,
    private val streamLocal: StreamDb
) : DeploymentRepository {

    override fun save(deployment: Deployment) {
        // Save image first before insert deployment
        if (deployment.images != null) {
            val images = deployment.images!!.map {
                imageLocal.insertWithResult(it)
            }
            deployment.images = realmList(images)
        }
        if (deployment.stream != null) {
            val stream = deployment.stream!!.let {
                streamLocal.insertWithResult(it)
            }
            deployment.stream = stream
        }
        deploymentLocal.insert(deployment)
    }

    override fun get(): Flow<List<Deployment>> {
        return deploymentLocal.getAsFlow()
    }
}
