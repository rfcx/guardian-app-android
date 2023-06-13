package org.rfcx.incidents.data.remote.guardian.image

import org.rfcx.incidents.entity.guardian.image.DeploymentImage

/**
 * DeviceAPI response for getting a deployment asset
 */
data class DeploymentAssetResponse(
    var id: String = "",
    var mimeType: String = "",
    var meta: AssetMeta?
) {
    fun toDeploymentImage(): DeploymentImage {
        return DeploymentImage(
            remotePath = "assets/$id"
        )
    }
}

data class AssetMeta(
    val label: String
)
