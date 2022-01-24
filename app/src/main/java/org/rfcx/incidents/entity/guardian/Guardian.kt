package org.rfcx.incidents.entity.guardian

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * A device that listens in the forest
 */

open class Guardian : RealmObject() {
    @PrimaryKey
    var guid: String = ""
    var name: String = ""
}
