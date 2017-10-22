package android.rfcx.org.ranger.entity

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject

open class Event : RealmObject() {

    @SerializedName("event_guid")
    var eventGUID: String = ""

    @SerializedName("audio_guid")
    var audioGUID: String = ""

    var latitude: Float = 0.0f
    var longitude: Float = 0.0f

    @SerializedName("begins_at")
    var beginsAt: String = ""

    @SerializedName("ends_at")
    var endAt: String = ""

    var type: String = ""
    var value: String = ""
    var confidence: Float = 0.0f

    @SerializedName("guardian_guid")
    var guardianGUID: String = ""

    @SerializedName("guardian_shortname")
    var guardianShortname: String = ""

    var site: String = ""
    var timezone: String = ""

}