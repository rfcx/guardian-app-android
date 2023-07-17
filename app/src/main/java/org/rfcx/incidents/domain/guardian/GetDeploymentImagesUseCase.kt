package org.rfcx.incidents.domain.guardian

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.deploy.DeploymentRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.base.FlowWithParamUseCase
import org.rfcx.incidents.entity.guardian.image.DeploymentImage

class GetDeploymentImagesUseCase(
    private val deploymentRepository: DeploymentRepository
) : FlowWithParamUseCase<GetDeploymentImagesParams, Result<List<DeploymentImage>>>() {
    override fun performAction(param: GetDeploymentImagesParams): Flow<Result<List<DeploymentImage>>> {
        return deploymentRepository.listImages(param.deploymentId)
    }
}

data class GetDeploymentImagesParams(
    val deploymentId: Int
)
