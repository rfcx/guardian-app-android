package org.rfcx.incidents.domain.guardian.deploy

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.deploy.DeploymentRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.base.FlowWithParamUseCase

class DeployDeploymentUseCase(private val deploymentRepository: DeploymentRepository) : FlowWithParamUseCase<DeploymentDeployParams, Result<String>>() {
    override fun performAction(param: DeploymentDeployParams): Flow<Result<String>> {
        return deploymentRepository.upload(param.streamId)
    }
}

data class DeploymentDeployParams(
    val streamId: Int
)
