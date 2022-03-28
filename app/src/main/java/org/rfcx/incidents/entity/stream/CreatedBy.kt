package org.rfcx.incidents.entity.stream

import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class CreatedBy(
    var user: User? = null
) : RealmObject() {
    companion object {
        const val TABLE_NAME = "CreatedBy"
        const val RESPONSES_USER = "user"
    }
}
