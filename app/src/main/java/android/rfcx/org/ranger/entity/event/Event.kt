package android.rfcx.org.ranger.entity.event

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import java.util.*

/**
 * Created by Jingjoeh on 10/5/2017 AD.
 */


open class Event : RealmObject() {

    @SerializedName("event_guid")
    var eventGUID: String = ""

    @SerializedName("audio_guid")
    var audioGUID: String = ""

    var latitude: String = ""
    var longitude: String = ""

    @SerializedName("begins_at")
    var beginsAt: Date? = null

    @SerializedName("ends_at")
    var endAt: Date? = null

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