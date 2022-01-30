package org.rfcx.incidents.entity.report

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.text.DecimalFormat
import java.util.*

open class Report(
    @PrimaryKey
    @Expose(serialize = false)
    var id: Int = 0, // local id
    @Expose(serialize = false)
    var guid: String? = null, // remote id as stored on the server e.g. ec4dfe81-f10c-9592-b47a-14aada85112b
    
    @SerializedName("value")
    var value: String = "", // type of report: chainsaw, vehicle, trespasser, gunshot, other
    @SerializedName("site")
    var site: String = "", // shortname of the site
    @SerializedName("reported_at")
    var reportedAt: Date = Date(), // timestamp of when the report was created
    @SerializedName("lat")
    var latitude: Double = 0.0,
    @SerializedName("long")
    var longitude: Double = 0.0,
    @SerializedName("age_estimate")
    var ageEstimateRaw: Int = 0,  // 0 now, 10 last 24 hours, 20 last week, 30 last month
    @SerializedName("notes")
    var notes: String? = null,
    @Expose(serialize = false)
    var audioLocation: String? = null, // unused on server
    @Expose(serialize = false)
    var syncState: Int = 0 // 0 unsent, 1 uploading, 2 uploaded (sync complete)
) : RealmObject() {
    
    fun getLatLng(): String {
        val decimalFormat = DecimalFormat("##.######")
        
        val lat = decimalFormat.format(latitude)
        val lng = decimalFormat.format(longitude)
        
        return "$lat, $lng"
    }
    
    fun getAgeEstimate(): AgeEstimate {
        return AgeEstimate.fromInt(ageEstimateRaw) ?: AgeEstimate.NONE
    }
    
    companion object {
        const val FIELD_GUID = "guid"
        const val FIELD_ID = "id"
    }
    
    enum class AgeEstimate(val value: Int) {
        NONE(-1), LAST_MONTH(30), LAST_WEEK(20),
        LAST_24_HR(10), NOW(0);
        
        companion object {
            private val map = AgeEstimate.values().associateBy(AgeEstimate::value)
            fun fromInt(ageEstimate: Int) = map[ageEstimate]
        }
    }
}
