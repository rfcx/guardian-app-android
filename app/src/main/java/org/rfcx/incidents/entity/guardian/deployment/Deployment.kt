package org.rfcx.incidents.entity.guardian.deployment

import android.content.Context
import com.google.gson.annotations.Expose
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import org.rfcx.incidents.entity.guardian.image.DeploymentImage
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.randomDeploymentId
import org.rfcx.incidents.view.report.deployment.MapMarker
import java.io.Serializable
import java.util.Date

@RealmClass
open class Deployment(
    @PrimaryKey
    var id: Int = 0,
    var externalId: String? = null,
    var deployedAt: Date = Date(),
    var deploymentKey: String = randomDeploymentId(),
    @Expose(serialize = false)
    var createdAt: Date = Date(),
    var isActive: Boolean = false,
    @Expose(serialize = false)
    var syncState: Int = 0,
    var deviceParameters: String? = null,
    var images: RealmList<DeploymentImage>? = null
) : RealmModel, Serializable {

    fun areAllImagesSynced(): Boolean {
        return images?.find { it.syncState != SyncState.SENT.value } == null
    }

    fun getAllImagesState(): Int {
        if (images == null) return SyncState.SENT.value
        if (images!!.none { it.syncState != SyncState.SENT.value }) return SyncState.SENT.value
        if (images!!.any { it.syncState == SyncState.SENDING.value }) return SyncState.SENDING.value
        if (images!!.any { it.syncState == SyncState.UNSENT.value }) return SyncState.UNSENT.value
        return SyncState.SENT.value
    }
    companion object {
        const val TABLE_NAME = "Deployment"
        const val FIELD_ID = "id"
        const val FIELD_EXTERNAL_ID = "externalId"
        const val FIELD_SYNC_STATE = "syncState"
        const val FIELD_STREAM = "stream"
        const val FIELD_DEVICE_PARAMETERS = "deviceParameters"
        const val FIELD_DEPLOYED_AT = "deployedAt"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_DEPLOYMENT_KEY = "deploymentKey"
        const val FIELD_IS_ACTIVE = "isActive"
        const val FIELD_IMAGES = "images"
    }
}
