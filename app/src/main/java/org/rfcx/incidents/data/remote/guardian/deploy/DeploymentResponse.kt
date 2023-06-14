package org.rfcx.incidents.data.remote.guardian.deploy

import com.google.gson.JsonElement
import org.rfcx.incidents.data.remote.streams.StreamResponse
import org.rfcx.incidents.entity.guardian.deployment.Deployment
import org.rfcx.incidents.entity.response.SyncState
import java.util.Date

data class DeploymentResponse(
    var id: String? = null,
    var deploymentType: String? = null,
    var deployedAt: Date? = null,
    var stream: StreamResponse? = null,
    var createdAt: Date? = null,
    var updatedAt: Date? = null,
    var deletedAt: Date? = null,
    var deviceParameters: JsonElement? = null
)

data class DeploymentsResponse(
    var id: String? = null,
    var deploymentType: String? = null,
    var deployedAt: Date? = null,
    var streamId: String? = null,
    var createdAt: Date? = null,
    var updatedAt: Date? = null,
    var deletedAt: Date? = null,
    var deviceParameters: JsonElement? = null
) {
    fun toDeployment(): Deployment {
        return Deployment(
            id = 0,
            externalId = id,
            deploymentKey = id!!,
            deployedAt = deployedAt ?: Date(),
            createdAt = createdAt ?: Date(),
            syncState = SyncState.SENT.value,
            deviceParameters = deviceParameters.toString()
        )
    }
}
