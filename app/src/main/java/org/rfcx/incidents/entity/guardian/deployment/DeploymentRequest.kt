package org.rfcx.incidents.entity.guardian.deployment

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.toISO8601Format
import java.util.*

data class DeploymentRequest(
    var deploymentKey: String,
    var deploymentType: String,
    var deployedAt: String = Date().toISO8601Format(),
    var stream: StreamRequest? = null,
    var deviceParameters: JsonObject? = null
)

fun Stream.toDeploymentRequestBody(): DeploymentRequest {
    return DeploymentRequest(
        deploymentKey = this.deployment!!.deploymentKey,
        deploymentType = "guardian",
        deployedAt = this.deployment!!.deployedAt.toISO8601Format(),
        stream = this.toRequestBody(),
        deviceParameters = Gson().fromJson(this.deployment!!.deviceParameters, JsonObject::class.java)
    )
}
