package org.rfcx.incidents.data.interfaces.guardian.deploy

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.guardian.deploy.GetStreamWithDeploymentAndIncidentParams
import org.rfcx.incidents.entity.stream.Stream

interface DeploymentAndIncidentRepository {
    fun get(params: GetStreamWithDeploymentAndIncidentParams): Flow<Result<List<Stream>>>
}
