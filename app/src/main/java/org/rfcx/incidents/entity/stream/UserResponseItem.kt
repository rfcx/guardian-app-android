package org.rfcx.incidents.entity.stream

import io.realm.RealmObject

open class UserResponseItem(
    var firstname: String = ""
) : RealmObject() {
    companion object {
        const val TABLE_NAME = "UserResponseItem"
        const val FIRSTNAME = "firstname"
    }
}
