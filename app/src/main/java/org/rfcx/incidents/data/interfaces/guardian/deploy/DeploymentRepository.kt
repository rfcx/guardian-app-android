package org.rfcx.incidents.data.interfaces.guardian.deploy

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.entity.guardian.deployment.Deployment
import org.rfcx.incidents.data.remote.common.Result

interface DeploymentRepository {
    fun save(deployment: Deployment)

    fun get(): Flow<List<Deployment>>

    fun upload(id: Int): Flow<Result<Boolean>>
}
