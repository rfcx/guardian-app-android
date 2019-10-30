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
	var confidence: Double = 0.0
	@SerializedName("start")
	var start: Int = 0
	@SerializedName("end")
	var end: Int = 0
	
	constructor(parcel: Parcel) : this() {
		guid = parcel.readString().toString()
		confidence = parcel.readDouble()
		start = parcel.readInt()
		end = parcel.readInt()
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
