package org.rfcx.incidents.data.remote.guardian.deploy

import com.google.gson.JsonElement
import org.rfcx.incidents.data.remote.streams.StreamResponse
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
