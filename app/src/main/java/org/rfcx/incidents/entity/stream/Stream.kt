package org.rfcx.incidents.entity.stream

import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import org.rfcx.incidents.entity.response.SyncState
import java.io.Serializable
import java.util.TimeZone

@RealmClass
open class Stream(
    @PrimaryKey
    var id: Int = -1,
    var name: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var altitude: Double = 0.0,
    @SerializedName("timezone")
    var timezoneRaw: String = "",
    var projectId: String = "",
    var tags: RealmList<String>? = null,
    var lastIncident: Incident? = null,
    var guardianType: String? = null,
    var order: Int = Int.MAX_VALUE,
    var externalId: String? = null,
    var syncState: Int = SyncState.UNSENT.value
) : RealmModel, Serializable {

    val timezone: TimeZone get() = TimeZone.getTimeZone(this.timezoneRaw)

    companion object {
        const val TABLE_NAME = "Stream"
        const val FIELD_ID = "id"
        const val FIELD_PROJECT_ID = "projectId"
        const val FIELD_ORDER = "order"
        const val TAG_HOT = "hot"
        const val TAG_RECENT = "recent"
        const val TAG_TIMEZONE_RAW = "timezoneRaw"
        const val FIELD_EXTERNAL_ID = "externalId"
        const val FIELD_SYNC_STATE = "syncState"
        const val FIELD_ALTITUDE = "altitude"
    }
}
