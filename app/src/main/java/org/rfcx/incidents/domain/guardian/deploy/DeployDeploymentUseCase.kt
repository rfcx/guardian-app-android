package org.rfcx.incidents.domain.guardian.deploy

import org.rfcx.incidents.data.interfaces.guardian.deploy.DeploymentRepository
import org.rfcx.incidents.domain.base.NoResultWithParamUseCase
import org.rfcx.incidents.entity.guardian.deployment.Deployment

class DeployDeploymentUseCase(private val deploymentRepository: DeploymentRepository) : NoResultWithParamUseCase<DeploymentDeployParams>() {
    override fun performAction(param: DeploymentDeployParams) {
        deploymentRepository.deploy(param.deployment)
    }
}

data class DeploymentDeployParams(
    val deployment: Deployment
)
