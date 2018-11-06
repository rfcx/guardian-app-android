package org.rfcx.ranger.entity.event

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Event() : RealmObject(), Parcelable {
	@PrimaryKey
	var event_guid: String = ""
	
	@SerializedName("audio_guid")
	var audioGUID: String? = ""
	
	var latitude: Double? = null
	var longitude: Double? = null
	
	@SerializedName("begins_at")
	var beginsAt: String? = ""
	
	@SerializedName("ends_at")
	var endAt: String? = ""
	
	var type: String? = ""
	var value: String? = ""
	var confidence: Float? = null
	
	@SerializedName("guardian_guid")
	var guardianGUID: String? = ""
	
	@SerializedName("guardian_shortname")
	var guardianShortname: String? = ""
	
	var site: String? = ""
	var timezone: String? = ""
	
	var isOpened: Boolean = false
	var isConfirmed: Boolean = false
	var audio: Audio? = null
	
	constructor(parcel: Parcel) : this() {
		event_guid = parcel.readString()
		audioGUID = parcel.readString()
		latitude = parcel.readValue(Double::class.java.classLoader) as? Double
		longitude = parcel.readValue(Double::class.java.classLoader) as? Double
		beginsAt = parcel.readString()
		endAt = parcel.readString()
		type = parcel.readString()
		value = parcel.readString()
		confidence = parcel.readFloat()
		guardianGUID = parcel.readString()
		guardianShortname = parcel.readString()
		site = parcel.readString()
		timezone = parcel.readString()
		isOpened = parcel.readByte() != 0.toByte()
		isConfirmed = parcel.readByte() != 0.toByte()
		audio = parcel.readParcelable(Audio::class.java.classLoader)
	}
	
	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(event_guid)
		parcel.writeString(audioGUID)
		parcel.writeValue(latitude)
		parcel.writeValue(longitude)
		parcel.writeString(beginsAt)
		parcel.writeString(endAt)
		parcel.writeString(type)
		parcel.writeString(value)
		parcel.writeValue(confidence)
		parcel.writeString(guardianGUID)
		parcel.writeString(guardianShortname)
		parcel.writeString(site)
		parcel.writeString(timezone)
		parcel.writeByte(if (isOpened) 1 else 0)
		parcel.writeByte(if (isConfirmed) 1 else 0)
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
		
		const val eventGUID = "event_guid"
		// Event value
		const val chainsaw = "chainsaw"
		const val gunshot = "gunshot"
		const val vehicle = "vehicle"
		const val trespasser = "trespasser"
		const val other = "other"
		
	}
}