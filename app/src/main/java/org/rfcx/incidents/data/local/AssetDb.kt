package org.rfcx.incidents.data.local

import io.realm.Realm
import org.rfcx.incidents.entity.response.Asset

class AssetDb(val realm: Realm) {

    fun save(asset: Asset) {
        realm.executeTransaction {
            val assets = it.where(Asset::class.java).equalTo(Asset.ASSET_ID, asset.id).findFirst()
            if (assets == null) {
                val id = (it.where(Asset::class.java).max(Asset.ASSET_ID)?.toInt() ?: 0) + 1
                asset.id = id
                it.insertOrUpdate(asset)
            }
        }
    }
}
