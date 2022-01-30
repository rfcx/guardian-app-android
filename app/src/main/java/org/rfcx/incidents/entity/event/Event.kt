package org.rfcx.incidents.entity.event

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Event : RealmObject() {

    @PrimaryKey
    var id: String = ""

    companion object {
        const val chainsaw = "chainsaw"
        const val gunshot = "gunshot"
        const val vehicle = "vehicle"
        const val trespasser = "trespasser"
        const val other = "other"
    }
}
