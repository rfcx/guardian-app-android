package org.rfcx.incidents.entity.stream

import io.realm.RealmObject
import org.rfcx.incidents.data.remote.streams.UserResponse

open class ResponseItem(
    var id: String? = null,
    var userResponseItem: UserResponseItem? = null
) : RealmObject() {
    companion object {
        const val TABLE_NAME = "ResponseItem"
        const val RESPONSES_ID = "id"
        const val RESPONSES_USER_RESPONSE_ITEM = "userResponseItem"
    }
}
