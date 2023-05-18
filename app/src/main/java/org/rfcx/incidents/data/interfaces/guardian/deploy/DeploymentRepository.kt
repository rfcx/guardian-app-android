package org.rfcx.incidents.data.interfaces.guardian.deploy

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.entity.guardian.deployment.Deployment

interface DeploymentRepository {
    fun save(deployment: Deployment)

    fun get(): Flow<List<Deployment>>
}
