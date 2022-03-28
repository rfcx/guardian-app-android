package org.rfcx.incidents.entity.stream

import io.realm.RealmObject

open class User(
    var firstname: String = ""
) : RealmObject() {
    companion object {
        const val TABLE_NAME = "User"
        const val FIRSTNAME = "firstname"
    }
}
