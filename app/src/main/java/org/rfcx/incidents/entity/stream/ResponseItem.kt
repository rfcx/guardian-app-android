package org.rfcx.incidents.entity.stream

import io.realm.RealmObject

open class ResponseItem(
    var id: String = ""
) : RealmObject() {
    companion object {
        const val TABLE_NAME = "ResponseItem"
        const val RESPONSES_ID = "id"
    }
}
