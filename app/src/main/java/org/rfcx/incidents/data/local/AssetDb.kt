package org.rfcx.incidents.data.local

import io.realm.Realm
import org.rfcx.incidents.entity.response.Asset
import org.rfcx.incidents.entity.response.SyncState

class AssetDb(val realm: Realm) {
    fun save(asset: Asset): Asset {
        realm.executeTransaction {
            if (asset.id == 0) {
                asset.id = (it.where(Asset::class.java).max("id")?.toInt() ?: 0) + 1
            }
            it.insertOrUpdate(asset)
        }
        return asset
    }

    fun delete(assetId: Int) {
        val shouldDelete = realm.where(Asset::class.java).equalTo(Asset.ASSET_ID, assetId).findFirst()
        realm.executeTransaction { shouldDelete?.deleteFromRealm() }
    }

    fun unsentCount(): Long {
        return realm.where(Asset::class.java).notEqualTo(Asset.ASSET_SYNC_STATE, SyncState.SENT.value).count()
    }

    fun unlockSending() {
        realm.executeTransaction { it ->
            val snapshot = it.where(Asset::class.java).equalTo(Asset.ASSET_SYNC_STATE, SyncState.SENDING.value).findAll().createSnapshot()
            snapshot.forEach {
                it.syncState = SyncState.UNSENT.value
            }
        }
    }

    fun lockUnsent(): List<Asset> {
        var unsentCopied: List<Asset> = listOf()
        realm.executeTransaction { it ->
            val unsent = it.where(Asset::class.java)
                .equalTo(Asset.ASSET_SYNC_STATE, SyncState.UNSENT.value)
                .isNotNull(Asset.ASSET_SERVER_ID)
                .findAll().createSnapshot()
            unsentCopied = unsent.toList()
            unsent.forEach {
                it.syncState = SyncState.SENDING.value
            }
        }
        return unsentCopied
    }

    fun markSent(id: Int, remotePath: String?) {
        mark(id = id, syncState = SyncState.SENT.value, remotePath)
    }

    fun markUnsent(id: Int) {
        mark(id = id, syncState = SyncState.UNSENT.value, null)
    }

    private fun mark(id: Int, syncState: Int, remotePath: String?) {
        realm.executeTransaction {
            val asset = it.where(Asset::class.java).equalTo(Asset.ASSET_ID, id).findFirst()
            if (asset != null) {
                asset.syncState = syncState
                asset.remotePath = remotePath
            }
        }
    }

    fun saveReportServerId(serverId: String, assetId: Int) {
        val asset = realm.where(Asset::class.java)
            .equalTo(Asset.ASSET_ID, assetId).findFirst()

        if (asset != null) {
            realm.executeTransaction { transaction ->
                asset.serverId = serverId
                transaction.insertOrUpdate(asset)
            }
        }
    }
}
