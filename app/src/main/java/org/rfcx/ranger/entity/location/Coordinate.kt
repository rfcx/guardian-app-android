package org.rfcx.ranger.entity.location

import io.realm.RealmList
import io.realm.RealmObject

open class Coordinate(
		var latitude: Double = 0.0,
		var longitude: Double = 0.0,
		var altitude: Double = 0.0
) : RealmObject() {
	companion object {
		const val TABLE_NAME = "Coordinate"
		const val COORDINATE_LATITUDE = "latitude"
		const val COORDINATE_LONGITUDE = "longitude"
		const val COORDINATE_ALTITUDE = "altitude"
	}
}

fun Coordinate.toDoubleArray(): DoubleArray {
	return listOf(this.latitude, this.longitude, this.altitude).toDoubleArray()
}

fun RealmList<Coordinate>.toListDoubleArray(): List<DoubleArray> {
	return this.map {
		listOf(it.longitude, it.latitude).toDoubleArray()
	}.toList()
}
