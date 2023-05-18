package org.rfcx.incidents.domain.guardian.deploy

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.deploy.DeploymentRepository
import org.rfcx.incidents.domain.base.FlowUseCase
import org.rfcx.incidents.entity.guardian.deployment.Deployment

class GetDeploymentsUseCase(
    private val deploymentRepository: DeploymentRepository
) : FlowUseCase<List<Deployment>>() {
    override fun performAction(): Flow<List<Deployment>> {
        return deploymentRepository.get()
    }
}

