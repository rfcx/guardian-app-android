package org.rfcx.incidents.domain.guardian.deploy

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.deploy.DeploymentRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.base.FlowWithParamUseCase
import org.rfcx.incidents.entity.stream.Stream

class GetStreamsWithDeploymentUseCase(
    private val deploymentRepository: DeploymentRepository
) : FlowWithParamUseCase<GetStreamWithDeploymentParams, Result<List<Stream>>>() {
    override fun performAction(param: GetStreamWithDeploymentParams): Flow<Result<List<Stream>>> {
        return deploymentRepository.get(param)
    }
}

data class GetStreamWithDeploymentParams(
    val projectId: String?,
    val forceRefresh: Boolean
)
