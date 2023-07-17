package org.rfcx.incidents.domain.guardian.detail

import org.rfcx.incidents.data.interfaces.guardian.deploy.DeploymentRepository
import org.rfcx.incidents.domain.base.NoResultWithParamUseCase
import org.rfcx.incidents.view.guardian.checklist.photos.Image

class AddImageToDeploymentUseCase(
    private val deploymentRepository: DeploymentRepository
) : NoResultWithParamUseCase<AddImageParams>() {
    override fun performAction(param: AddImageParams) {
        deploymentRepository.addImages(param.deploymentId, param.images)
    }
}

data class AddImageParams(
    val deploymentId: Int,
    val images: List<Image>
)
