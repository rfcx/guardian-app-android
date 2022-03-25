package org.rfcx.incidents.entity.stream

import io.realm.RealmObject

open class ResponseItem(
    var id: String? = null,
    var createdBy: String? = null
) : RealmObject() {
    companion object {
        const val TABLE_NAME = "ResponseItem"
        const val RESPONSES_ID = "id"
        const val RESPONSES_CREATED_BY = "createdBy"
    }
}
