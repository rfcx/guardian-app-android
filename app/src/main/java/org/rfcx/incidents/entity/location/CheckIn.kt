package org.rfcx.incidents.entity.location

import com.google.gson.annotations.Expose
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.text.DecimalFormat
import java.util.*

open class CheckIn(
    var time: Date = Date(),
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    @PrimaryKey
    @Expose(serialize = false)
    var id: Int = 0,
    @Expose(serialize = false)
    var timestamp: Long = System.currentTimeMillis(),
    @Expose(serialize = false)
    var synced: Boolean = false
) : RealmObject() {
    fun getLatLng(): String {
        val decimalFormat = DecimalFormat("##.######")

        val lat = decimalFormat.format(latitude)
        val lng = decimalFormat.format(longitude)

        return "$lat, $lng"
    }
}
