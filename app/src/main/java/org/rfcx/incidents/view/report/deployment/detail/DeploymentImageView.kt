package org.rfcx.incidents.view.report.deployment.detail

import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.guardian.image.DeploymentImage
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.view.guardian.checklist.photos.Image
import org.rfcx.incidents.view.guardian.checklist.photos.ImageType

data class DeploymentImageView(
    val id: Int,
    val localPath: String,
    val remotePath: String?,
    val label: String,
    var syncState: Int = 0 // syncToFireStoreState
) {
    val syncImage = when (syncState) {
        SyncState.UNSENT.value -> R.drawable.ic_cloud_queue
        SyncState.SENDING.value -> R.drawable.ic_cloud_upload
        else -> R.drawable.ic_cloud_done
    }
}

/**
 * @param syncState Return when wait upload, uploading to Firebase Storage and when uploaded to Firestore
 * */
fun DeploymentImage.toDeploymentImageView(): DeploymentImageView {
    return DeploymentImageView(
        id = this.id,
        localPath = this.localPath,
        remotePath = if (this.remotePath != null) BuildConfig.DEVICE_API_BASE_URL + this.remotePath else null,
        label = this.imageLabel,
        syncState = this.syncState
    )
}

fun DeploymentImage.toImage(): Image {
    return Image(
        this.id,
        this.imageLabel,
        if (this.imageLabel == "other") ImageType.OTHER else ImageType.NORMAL,
        this.localPath,
        this.remotePath,
        false
    )
}
