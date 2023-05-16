package org.rfcx.incidents.entity.guardian.deployment

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.rfcx.incidents.util.toISO8601Format
import java.util.*

data class DeploymentRequest(
    var deploymentKey: String,
    var deploymentType: String,
    var deployedAt: String = Date().toISO8601Format(),
    var stream: StreamRequest? = null,
    var deviceParameters: JsonObject? = null
)

fun Deployment.toRequestBody(): DeploymentRequest {
    return DeploymentRequest(
        deploymentKey = this.deploymentKey,
        deploymentType = "guardian",
        deployedAt = this.deployedAt.toISO8601Format(),
        stream = if (this.stream?.name == null) null else this.stream?.toRequestBody(),
        deviceParameters = Gson().fromJson(this.deviceParameters, JsonObject::class.java)
    )
}
