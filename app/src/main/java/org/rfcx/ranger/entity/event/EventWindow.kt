package org.rfcx.ranger.entity.event

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class EventWindow() : RealmModel, Parcelable {
	
	@PrimaryKey
	@SerializedName("guid")
	var guid: String = ""
	@SerializedName("confidence")
	var confidence: Double? = null
	@SerializedName("start")
	var start: Int? = null
	@SerializedName("end")
	var end: Int? = null
	
	constructor(parcel: Parcel) : this() {
		guid = parcel.readString().toString()
		confidence = parcel.readValue(Double::class.java.classLoader) as? Double
		start = parcel.readValue(Int::class.java.classLoader) as? Int
		end = parcel.readValue(Int::class.java.classLoader) as? Int
	}
	
	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(guid)
		parcel.writeValue(confidence)
		parcel.writeValue(start)
		parcel.writeValue(end)
	}
	
	override fun describeContents(): Int {
		return 0
	}
	
	companion object CREATOR : Parcelable.Creator<EventWindow> {
		override fun createFromParcel(parcel: Parcel): EventWindow {
			return EventWindow(parcel)
		}
		
		override fun newArray(size: Int): Array<EventWindow?> {
			return arrayOfNulls(size)
		}
	}
}
