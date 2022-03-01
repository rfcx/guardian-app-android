package org.rfcx.incidents.entity.response

import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import io.realm.RealmObject
import java.util.Date

open class Asset(
    @PrimaryKey
    var id: Int = 0,
    @Expose(serialize = false)
    var type: String = AssetType.IMAGE.value,
    var serverId: String? = null,
    @Expose(serialize = false)
    var createdAt: Date = Date(),
    @Expose(serialize = false)
    var localPath: String = "", // Path on the device
    var syncState: Int = SyncState.UNSENT.value,
    var remotePath: String? = null // image url after synced to server
) : RealmObject() {
    companion object {
        const val TABLE_NAME = "Asset"
        const val ASSET_ID = "id"
        const val ASSET_TYPE = "type"
        const val ASSET_SERVER_ID = "serverId"
        const val ASSET_CREATED_AT = "createdAt"
        const val ASSET_LOCAL_PATH = "localPath"
        const val ASSET_SYNC_STATE = "syncState"
        const val ASSET_REMOTE_PATH = "remotePath"
    }
}
