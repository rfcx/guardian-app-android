package org.rfcx.incidents.data.local

import io.realm.Realm
import org.rfcx.incidents.entity.response.Asset

class AssetDb(val realm: Realm) {
    fun save(asset: Asset): Asset {
        realm.executeTransaction {
            if (asset.id == 0) {
                asset.id = (it.where(Asset::class.java).max("id")?.toInt() ?: 0) + 1
                it.insertOrUpdate(asset)
            }
        }
        return asset
    }

    fun getA(): List<Asset> = realm.where(Asset::class.java).findAll()
}
