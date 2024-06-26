package org.rfcx.incidents.entity.response

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.Date

open class Asset(
    @PrimaryKey
    var id: Int = 0,
    var typeRaw: String = AssetType.IMAGE.value,
    var serverId: String? = null,
    var createdAt: Date = Date(),
    var localPath: String = "", // Path on the device
    var syncState: Int = SyncState.UNSENT.value,
    var remotePath: String? = null // image url after synced to server
) : RealmObject() {

    val type: AssetType?
        get() = when (typeRaw) {
            AssetType.IMAGE.value -> AssetType.IMAGE
            AssetType.AUDIO.value -> AssetType.AUDIO
            AssetType.KML.value -> AssetType.KML
            else -> null
        }

    companion object {
        const val TABLE_NAME = "Asset"
        const val ASSET_ID = "id"
        const val ASSET_TYPE_RAW = "typeRaw"
        const val ASSET_SERVER_ID = "serverId"
        const val ASSET_CREATED_AT = "createdAt"
        const val ASSET_LOCAL_PATH = "localPath"
        const val ASSET_SYNC_STATE = "syncState"
        const val ASSET_REMOTE_PATH = "remotePath"
    }
}
