package org.rfcx.incidents.data.local.deploy

import io.realm.Realm
import org.rfcx.incidents.entity.guardian.image.DeploymentImage
import org.rfcx.incidents.entity.response.SyncState

class DeploymentImageDb(private val realm: Realm) {

    fun getByIds(ids: List<Int>): List<DeploymentImage> {
        return realm.where(DeploymentImage::class.java).`in`(DeploymentImage.FIELD_ID, ids.toTypedArray()).findAll()
    }

    fun insert(image: DeploymentImage) {
        realm.executeTransaction {
            if (image.remotePath != null) {
                val existingImage = realm.where(DeploymentImage::class.java).equalTo(DeploymentImage.FIELD_REMOTE_PATH, image.remotePath).findFirst()
                if (existingImage == null) {
                    val id = (realm.where(DeploymentImage::class.java).max(DeploymentImage.FIELD_ID)?.toInt() ?: 0) + 1
                    image.id = id
                    it.copyToRealmOrUpdate(image)
                } else {
                    image.id = existingImage.id
                    it.copyToRealmOrUpdate(image)
                }
            } else {
                if (image.id == 0) {
                    val id = (realm.where(DeploymentImage::class.java).max(DeploymentImage.FIELD_ID)?.toInt() ?: 0) + 1
                    image.id = id
                    it.copyToRealmOrUpdate(image)
                } else {
                    it.copyToRealmOrUpdate(image)
                }
            }
        }
    }

    fun insertWithResult(image: DeploymentImage): DeploymentImage {
        insert(image)
        return realm.where(DeploymentImage::class.java).equalTo(DeploymentImage.FIELD_ID, image.id).findFirst()!!
    }

    fun markSent(id: Int, remotePath: String?) {
        realm.executeTransaction {
            val report = it.where(DeploymentImage::class.java).equalTo(DeploymentImage.FIELD_ID, id).findFirst()
            if (report != null) {
                report.syncState = SyncState.SENT.value
                report.remotePath = remotePath
            }
        }
    }

    /**
     * return DeploymentImage that not be sync to Firebase Storage
     */
    fun lockUnsent(id: Int) {
        realm.executeTransaction {
            val report = it.where(DeploymentImage::class.java).equalTo(DeploymentImage.FIELD_ID, id).findFirst()
            if (report != null) {
                report.syncState = SyncState.SENDING.value
            }
        }
    }

    /**
     * Mark DeploymentImage.syncState to Unsent
     */
    fun markUnsent(id: Int) {
        realm.executeTransaction {
            val report = it.where(DeploymentImage::class.java).equalTo(DeploymentImage.FIELD_ID, id).findFirst()
            if (report != null) {
                report.syncState = SyncState.UNSENT.value
            }
        }
    }

    fun unlockSending() {
        realm.executeTransaction { it ->
            val snapshot = it.where(DeploymentImage::class.java).equalTo(DeploymentImage.FIELD_SYNC_STATE, SyncState.SENDING.value).findAll().createSnapshot()
            snapshot.forEach {
                it.syncState = SyncState.UNSENT.value
            }
        }
    }

    fun unsentCount(): Long {
        return realm.where(DeploymentImage::class.java).notEqualTo(DeploymentImage.FIELD_SYNC_STATE, SyncState.SENT.value).count()
    }
}
