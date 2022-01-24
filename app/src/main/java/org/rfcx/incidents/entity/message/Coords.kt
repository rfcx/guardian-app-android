package org.rfcx.incidents.entity.message

import io.realm.RealmObject

open class Coords : RealmObject() {
    var lat: Double = 0.0
    var lon: Double = 0.0
}
