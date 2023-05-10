package org.rfcx.incidents.entity.guardian

import com.google.gson.annotations.Expose
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

@RealmClass
open class DeploymentImage(
    @PrimaryKey
    var id: Int = 0,
    @Expose(serialize = false)
    var deploymentId: Int = 0,
    var deploymentExternalId: String? = null,
    @Expose(serialize = false)
    var localPath: String = "",
    var remotePath: String? = null,
    var createdAt: Date = Date(),
    var imageLabel: String = "",
    @Expose(serialize = false)
    var syncState: Int = 0
) : RealmModel {
    companion object {
        const val TABLE_NAME = "DeploymentImage"
        const val FIELD_ID = "id"
        const val FIELD_DEPLOYMENT_ID = "deploymentId"
        const val FIELD_SYNC_STATE = "syncState"
        const val FIELD_REMOTE_PATH = "remotePath"
        const val FIELD_LOCAL_PATH = "localPath"
        const val FIELD_IMAGE_LABEL = "imageLabel"
        const val FIELD_DEPLOYMENT_EXTERNAL_ID = "deploymentExternalId"
        const val FIELD_CREATE_AT = "createdAt"
    }
}
