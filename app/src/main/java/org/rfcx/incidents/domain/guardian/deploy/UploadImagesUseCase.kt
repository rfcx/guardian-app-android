package org.rfcx.incidents.domain.guardian.deploy

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.deploy.DeploymentRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.base.FlowWithParamUseCase

class UploadImagesUseCase(private val deploymentRepository: DeploymentRepository) : FlowWithParamUseCase<UploadImagesParams, Result<Boolean>>() {
    override fun performAction(param: UploadImagesParams): Flow<Result<Boolean>> {
        return deploymentRepository.uploadImages(param.deploymentId)
    }
}

data class UploadImagesParams(
    val deploymentId: String
)
