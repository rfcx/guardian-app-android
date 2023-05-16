package org.rfcx.incidents.data.local.deploy

import io.realm.Realm
import org.rfcx.incidents.entity.guardian.image.DeploymentImage

class DeploymentImageDb(private val realm: Realm) {

    fun insert(image: DeploymentImage) {
        realm.executeTransaction {
            val existingImage = realm.where(DeploymentImage::class.java).equalTo(DeploymentImage.FIELD_REMOTE_PATH, image.remotePath).findFirst()
            if (existingImage == null) {
                val id = (realm.where(DeploymentImage::class.java).max(DeploymentImage.FIELD_ID)?.toInt() ?: 0) + 1
                image.id = id
                it.insert(image)
            } else {
                image.id = existingImage.id
                it.insertOrUpdate(image)
            }
        }
    }
}
