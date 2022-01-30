package org.rfcx.incidents.entity.response

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Voice(
    @PrimaryKey
    var id: Int = 0,
    var responseId: Int = 0,
    var responseServerId: String? = null,
    var localPath: String = "",
    var remotePath: String? = null,
    var syncState: Int = 0
) : RealmObject() {
    companion object {
        const val TABLE_NAME = "Voice"
        const val FIELD_ID = "id"
        const val FIELD_RESPONSE_ID = "responseId"
        const val FIELD_RESPONSE_SERVER_ID = "responseServerId"
        const val FIELD_LOCAL_PATH = "localPath"
        const val FIELD_REMOTE_PATH = "remotePath"
        const val FIELD_SYNC_STATE = "syncState"
    }
}
