package org.rfcx.ranger.entity

import org.rfcx.ranger.util.DateHelper
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by Jingjoeh on 10/23/2017 AD.
 */
open class RangerLocation : RealmObject() {

    companion object {
        val keyIsSent = "isSent"
    }

    @PrimaryKey
    var time: String = DateHelper.getIsoTime()
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var isSent: Boolean = false
}