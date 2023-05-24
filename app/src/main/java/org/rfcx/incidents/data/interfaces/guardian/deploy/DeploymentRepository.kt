package org.rfcx.incidents.data.interfaces.guardian.deploy

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.entity.guardian.deployment.Deployment
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.entity.stream.Stream

interface DeploymentRepository {
    fun save(stream: Stream)

    fun get(): Flow<List<Deployment>>

    fun upload(streamId: Int): Flow<Result<Boolean>>
}
