package org.rfcx.incidents.entity.location

import io.realm.RealmList
import io.realm.RealmObject
import java.util.*

open class Coordinate(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var altitude: Double = 0.0,
    var createdAt: Date = Date()
) : RealmObject()

fun List<Coordinate>.toListDoubleArray(): List<DoubleArray> {
    return this.map { doubleArrayOf(it.longitude, it.latitude) }
}

fun RealmList<Coordinate>.toListDoubleArray(): List<DoubleArray> {
    return this.map { doubleArrayOf(it.longitude, it.latitude) }
}
