package org.rfcx.incidents.entity.stream

import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.TimeZone

@RealmClass
open class Stream(
    @PrimaryKey
    var id: String = "",
    var name: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    @SerializedName("timezone")
    var timezoneRaw: String = "",
    var projectId: String = "",
    var tags: RealmList<String>? = null,
    var lastIncident: Incident? = null,
    var guardianType: String? = null,
    var order: Int = Int.MAX_VALUE,
    var isSynced: Boolean = false,
) : RealmModel {

    val timezone: TimeZone get() = TimeZone.getTimeZone(this.timezoneRaw)

    companion object {
        const val TABLE_NAME = "Stream"
        const val FIELD_ID = "id"
        const val FIELD_IS_SYNCED = "isSynced"
        const val FIELD_PROJECT_ID = "projectId"
        const val FIELD_ORDER = "order"
        const val TAG_HOT = "hot"
        const val TAG_RECENT = "recent"
        const val TAG_TIMEZONE_RAW = "timezoneRaw"
    }
}
