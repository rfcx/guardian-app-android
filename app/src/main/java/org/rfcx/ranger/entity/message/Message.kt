package org.rfcx.ranger.entity.message


import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Message : RealmObject() {
    @PrimaryKey
    var guid: String = ""
    var time: String = ""
    var text: String? = ""
    var type: String = ""
    var from: From? = null
    var to: To? = null
    var coords: Coords? = null
    var isOpened: Boolean = false

    companion object {
       val messageGUID = "guid"
    }
}