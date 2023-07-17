package org.rfcx.incidents.domain.guardian

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.deploy.DeploymentRepository
import org.rfcx.incidents.domain.base.FlowWithParamUseCase
import org.rfcx.incidents.entity.guardian.deployment.Deployment

class GetLocalLiveDeploymentUseCase(
    private val deploymentRepository: DeploymentRepository
) : FlowWithParamUseCase<GetLocalDeploymentParams, Deployment?>() {
    override fun performAction(param: GetLocalDeploymentParams): Flow<Deployment?> {
        return deploymentRepository.getByIdAsFlow(param.deploymentId)
    }
}

data class GetLocalDeploymentParams(
    val deploymentId: Int
)
