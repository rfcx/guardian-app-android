package org.rfcx.incidents.util

import android.location.Location
import android.location.LocationManager
import io.realm.kotlin.isValid
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.view.guardian.checklist.site.SiteWithDistanceItem

private fun findNearLocations(
    streamItems: List<Stream>,
    currentUserLocation: Location
): List<Pair<Stream, Float>>? {
    if (streamItems.isNotEmpty()) {
        // Find locate distances
        return streamItems.filter { it.isValid() }.map {
            val loc = Location(LocationManager.GPS_PROVIDER)
            loc.latitude = it.latitude
            loc.longitude = it.longitude
            val distance = loc.distanceTo(currentUserLocation) // return in meters
            Pair(it, distance)
        }
    }
    return null
}

fun getListSite(
    currentUserLocation: Location,
    streams: List<Stream>
): List<SiteWithDistanceItem> {

    val nearLocations =
        findNearLocations(
            streams,
            currentUserLocation
        )?.sortedBy { it.second }

    val locationsItems: List<SiteWithDistanceItem> =
        nearLocations?.map { it ->
            SiteWithDistanceItem(
                it.first,
                it.second
            )
        } ?: listOf()

    return locationsItems
}
