package org.rfcx.incidents.domain.guardian.deploy

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.deploy.DeploymentRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.base.FlowWithParamUseCase

class DeployDeploymentUseCase(private val deploymentRepository: DeploymentRepository) : FlowWithParamUseCase<DeploymentDeployParams, Result<Boolean>>() {
    override fun performAction(param: DeploymentDeployParams): Flow<Result<Boolean>> {
        return deploymentRepository.upload(param.id)
    }
}

data class DeploymentDeployParams(
    val id: Int
)
