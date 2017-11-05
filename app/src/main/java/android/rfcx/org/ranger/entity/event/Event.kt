package android.rfcx.org.ranger.entity.event

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Event() : RealmObject(), Parcelable {
    @PrimaryKey
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

    var isOpened: Boolean = false
    var isConfirmed: Boolean = false
    var audio: Audio? = null

    constructor(parcel: Parcel) : this() {
        eventGUID = parcel.readString()
        audioGUID = parcel.readString()
        latitude = parcel.readFloat()
        longitude = parcel.readFloat()
        beginsAt = parcel.readString()
        endAt = parcel.readString()
        type = parcel.readString()
        value = parcel.readString()
        confidence = parcel.readFloat()
        guardianGUID = parcel.readString()
        guardianShortname = parcel.readString()
        site = parcel.readString()
        timezone = parcel.readString()
        audio = parcel.readParcelable(Audio::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(eventGUID)
        parcel.writeString(audioGUID)
        parcel.writeFloat(latitude)
        parcel.writeFloat(longitude)
        parcel.writeString(beginsAt)
        parcel.writeString(endAt)
        parcel.writeString(type)
        parcel.writeString(value)
        parcel.writeFloat(confidence)
        parcel.writeString(guardianGUID)
        parcel.writeString(guardianShortname)
        parcel.writeString(site)
        parcel.writeString(timezone)
        parcel.writeParcelable(audio, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Event> {
        override fun createFromParcel(parcel: Parcel): Event {
            return Event(parcel)
        }

        override fun newArray(size: Int): Array<Event?> {
            return arrayOfNulls(size)
        }
    }

}