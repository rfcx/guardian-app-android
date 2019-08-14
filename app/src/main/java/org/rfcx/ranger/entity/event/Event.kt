package org.rfcx.ranger.entity.event

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Event() : RealmObject(), Parcelable {
	@PrimaryKey
	@SerializedName("event_guid")
	var event_guid: String = ""
	
	@SerializedName("audio_guid")
	var audioGUID: String? = ""
	
	@SerializedName("latitude")
	var latitude: Double? = null
	
	@SerializedName("longitude")
	var longitude: Double? = null
	
	@SerializedName("begins_at")
	var beginsAt: String? = ""
	
	@SerializedName("ends_at")
	var endAt: String? = ""
	@SerializedName("type")
	var type: String? = ""
	@SerializedName("value")
	var value: String? = ""
	@SerializedName("confidence")
	var confidence: Float? = null
	
	@SerializedName("guardian_guid")
	var guardianGUID: String? = ""
	
	@SerializedName("guardian_shortname")
	var guardianShortname: String? = ""
	
	@SerializedName("site")
	var site: String? = ""
	@SerializedName("timezone")
	var timezone: String? = ""
	
	var isOpened: Boolean = false
	@SerializedName("audio")
	var audio: Audio? = null
	@SerializedName("reviewerConfirmed")
	var reviewerConfirmed: Boolean? = null
	@SerializedName("ai_guid")
	var aiGuid: String? = ""
	
	constructor(parcel: Parcel) : this() {
		event_guid = parcel.readString() ?: ""
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
		audio = parcel.readParcelable(Audio::class.java.classLoader)
		reviewerConfirmed = parcel.readValue(Boolean::class.java.classLoader) as Boolean
		aiGuid = parcel.readString()
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
		parcel.writeParcelable(audio, flags)
		parcel.writeValue(reviewerConfirmed)
		parcel.writeString(aiGuid)
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