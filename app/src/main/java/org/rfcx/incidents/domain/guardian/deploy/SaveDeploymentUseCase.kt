package org.rfcx.incidents.domain.guardian.deploy

import org.rfcx.incidents.data.interfaces.guardian.deploy.DeploymentRepository
import org.rfcx.incidents.domain.base.NoResultWithParamUseCase
import org.rfcx.incidents.entity.stream.Stream

class SaveDeploymentUseCase(private val deploymentRepository: DeploymentRepository) : NoResultWithParamUseCase<DeploymentSaveParams>() {
    override fun performAction(param: DeploymentSaveParams) {
        deploymentRepository.save(param.stream)
    }
}

data class DeploymentSaveParams(
    val stream: Stream
)
