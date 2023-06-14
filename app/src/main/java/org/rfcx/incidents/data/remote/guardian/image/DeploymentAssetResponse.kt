package org.rfcx.incidents.data.remote.guardian.image

import org.rfcx.incidents.entity.guardian.image.DeploymentImage
import org.rfcx.incidents.entity.response.SyncState

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
            remotePath = "assets/$id",
            imageLabel = meta?.label ?: "other",
            syncState = SyncState.SENT.value
        )
    }
}

data class AssetMeta(
    val label: String
)
