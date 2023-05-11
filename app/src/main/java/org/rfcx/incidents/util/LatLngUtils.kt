package org.rfcx.incidents.util

import android.location.Location
import kotlin.math.absoluteValue

fun Double?.latitudeCoordinates(): String {
    val lat = this
    if (lat != null) {
        val directionLatitude = if (lat > 0) "N" else "S"
        val strLatitude = Location.convert(lat.absoluteValue, Location.FORMAT_DEGREES)
        return "${replaceDelimitersDD(strLatitude)}$directionLatitude"
    }
    return this.toString()
}

fun Double?.longitudeCoordinates(): String {
    val longitude = this
    if (longitude != null) {
        val directionLongitude = if (longitude > 0) "E" else "W"
        val strLongitude = Location.convert(longitude.absoluteValue, Location.FORMAT_DEGREES)
        return "${replaceDelimitersDD(strLongitude)}$directionLongitude"
    }
    return this.toString()
}

private fun replaceDelimitersDD(str: String): String {
    var strDDFormat = str
    val pointIndex = strDDFormat.indexOf(".")
    val endIndex = pointIndex + 6
    if (endIndex < strDDFormat.length) {
        strDDFormat = strDDFormat.substring(0, endIndex)
    }
    strDDFormat += "°"
    return strDDFormat
}
