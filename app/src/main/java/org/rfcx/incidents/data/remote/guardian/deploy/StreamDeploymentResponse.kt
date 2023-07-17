package org.rfcx.incidents.data.remote.guardian.deploy

import org.rfcx.incidents.data.remote.streams.ProjectResponse
import java.util.Date

data class StreamDeploymentResponse(
    var id: String? = null,
    var name: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var altitude: Double? = null,
    var createdAt: Date? = null,
    var updatedAt: Date? = null,
    var project: ProjectResponse? = null,
    var deployment: DeploymentResponse? = null
)
