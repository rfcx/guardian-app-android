package org.rfcx.incidents.entity.event

import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class Classification : RealmObject {
    var value: String = ""
    var title: String = ""

    constructor(value: String, title: String) {
        this.value = value
        this.title = title
    }

    constructor()
}
