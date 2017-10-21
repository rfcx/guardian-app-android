package android.rfcx.org.ranger.entity

import io.realm.RealmObject

/**
 * Created by Jingjoeh on 10/5/2017 AD.
 */


open class Coords : RealmObject() {
    var lat: Double = 0.0
    var lon: Double = 0.0
}