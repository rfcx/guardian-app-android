package org.rfcx.incidents.entity.stream

import io.realm.RealmObject

open class CreatedBy(
    var firstname: String? = null
) : RealmObject() {
    companion object {
        const val TABLE_NAME = "CreatedBy"
        const val FIRSTNAME = "firstname"
    }
}
