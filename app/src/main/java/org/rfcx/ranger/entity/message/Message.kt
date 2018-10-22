package org.rfcx.ranger.entity.message


import org.rfcx.ranger.entity.Coords
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by Jingjoeh on 10/21/2017 AD.
 */
open class Message : RealmObject() {
    @PrimaryKey
    var guid: String = ""
    var time: String = ""
    var text: String = ""
    var type: String = ""
    var from: From? = null
    var to: To? = null
    var coords: Coords? = null
    var isOpened: Boolean = false

    companion object {
       val  messageGUID = "guid"
    }
}