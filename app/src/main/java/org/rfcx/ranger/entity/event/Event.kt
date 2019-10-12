package org.rfcx.ranger.entity.event

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Event() : RealmObject(), Parcelable {
	@PrimaryKey
	@SerializedName("guid")
	var event_guid: String = ""
	
	@SerializedName("audioGuid")
	var audioGUID: String? = ""
	
	@SerializedName("latitude")
	var latitude: Double? = null
	
	@SerializedName("longitude")
	var longitude: Double? = null
	
	@SerializedName("begins_at")
	var beginsAt: Date = Date()
	
	@SerializedName("ends_at")
	var endAt: Date = Date()
	@SerializedName("type")
	var type: String? = ""
	@SerializedName("value")
	var value: String? = ""
	
	@SerializedName("audioMeasuredAt")
	var audioMeasuredAt: Long? = null
	
	@SerializedName("confidence")
	var confidence: Float? = null
	
	@SerializedName("guardianGuid")
	var guardianGUID: String? = ""
	
	@SerializedName("guardianShortname")
	var guardianShortname: String? = ""
	
	@SerializedName("siteGuid")
	var site: String? = ""
	@SerializedName("siteTimezone")
	var timezone: String? = ""
	
	var isOpened: Boolean = false
	@SerializedName("urls")
	var audio: Audio? = null
	@SerializedName("reviewer_confirmed")
	var reviewerConfirmed: Boolean? = null
	@SerializedName("aiGuid")
	var aiGuid: String? = ""
	
	constructor(parcel: Parcel) : this() {
		event_guid = parcel.readString() ?: ""
		audioGUID = parcel.readString()
		latitude = parcel.readValue(Double::class.java.classLoader) as? Double
		longitude = parcel.readValue(Double::class.java.classLoader) as? Double
		beginsAt = Date(parcel.readLong())
		endAt = Date(parcel.readLong())
		type = parcel.readString()
		value = parcel.readString()
		confidence = parcel.readValue(Float::class.java.classLoader) as? Float
		guardianGUID = parcel.readString()
		guardianShortname = parcel.readString()
		site = parcel.readString()
		timezone = parcel.readString()
		audio = parcel.readParcelable(Audio::class.java.classLoader)
		reviewerConfirmed = when (parcel.readInt()) {
			0 -> false
			1 -> true
			else -> null
		}
		aiGuid = parcel.readString()
	}
	
	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(event_guid)
		parcel.writeString(audioGUID)
		parcel.writeValue(latitude)
		parcel.writeValue(longitude)
		parcel.writeLong(beginsAt.time)
		parcel.writeLong(endAt.time)
		parcel.writeString(type)
		parcel.writeString(value)
		parcel.writeValue(confidence)
		parcel.writeString(guardianGUID)
		parcel.writeString(guardianShortname)
		parcel.writeString(site)
		parcel.writeString(timezone)
		parcel.writeParcelable(audio, flags)
		parcel.writeInt(when (reviewerConfirmed) {
			true -> 1
			false -> 0
			else -> -1
		})
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