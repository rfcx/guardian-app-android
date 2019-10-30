package org.rfcx.ranger.entity.event

import android.os.Parcel
import android.os.Parcelable
import io.realm.RealmObject

open class Audio() : RealmObject(), Parcelable{
    var opus: String = ""
    var png: String = ""

    constructor(parcel: Parcel) : this() {
        opus = parcel.readString() ?: ""
        png = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(opus)
        parcel.writeString(png)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Audio> {
        override fun createFromParcel(parcel: Parcel): Audio {
            return Audio(parcel)
        }

        override fun newArray(size: Int): Array<Audio?> {
            return arrayOfNulls(size)
        }
    }
}