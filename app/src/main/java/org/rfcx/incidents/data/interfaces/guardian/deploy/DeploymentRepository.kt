package org.rfcx.incidents.data.interfaces.guardian.deploy

import org.rfcx.incidents.entity.guardian.deployment.Deployment

interface DeploymentRepository {
    fun deploy(deployment: Deployment)
}
