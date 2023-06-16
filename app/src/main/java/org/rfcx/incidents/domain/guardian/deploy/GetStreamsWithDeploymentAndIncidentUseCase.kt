package org.rfcx.incidents.domain.guardian.deploy

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.deploy.DeploymentAndIncidentRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.base.FlowWithParamUseCase
import org.rfcx.incidents.entity.stream.Stream

class GetStreamsWithDeploymentAndIncidentUseCase(
    private val deploymentAndIncidentRepository: DeploymentAndIncidentRepository
) : FlowWithParamUseCase<GetStreamWithDeploymentAndIncidentParams, Result<List<Stream>>>() {
    override fun performAction(param: GetStreamWithDeploymentAndIncidentParams): Flow<Result<List<Stream>>> {
        return deploymentAndIncidentRepository.get(param)
    }
}

data class GetStreamWithDeploymentAndIncidentParams(
    val projectId: String,
    val forceRefresh: Boolean = false,
    val offset: Int = 0,
    val fromAlertUnsynced: Boolean = false
)
