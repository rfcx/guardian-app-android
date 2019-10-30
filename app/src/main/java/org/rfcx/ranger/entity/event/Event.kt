package org.rfcx.ranger.entity.event

import android.os.Parcel
import android.os.Parcelable
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Event() : RealmObject(), Parcelable {
	
	@PrimaryKey
	var id: String = ""
	var audioId: String = ""
	var latitude: Double? = null
	var longitude: Double? = null
	var beginsAt: Date = Date()
	var type: String? = ""
	var value: String = ""
	var label: String = ""
	var confirmedCount: Int = 0
	var rejectedCount: Int = 0
	var audioDuration: Long = 0
	var guardianId: String = ""
	var guardianName: String = ""
	var site: String = ""
	var audioOpusUrl: String = ""
	var audioPngUrl: String = ""
	
	var windows: RealmList<EventWindow> = RealmList()
	
	var reviewCreated: Date = Date()
	var reviewConfirmed: Boolean? = null
	
	constructor(parcel: Parcel) : this() {
		id = parcel.readString() ?: ""
		audioId = parcel.readString() ?: ""
		latitude = parcel.readValue(Double::class.java.classLoader) as? Double
		longitude = parcel.readValue(Double::class.java.classLoader) as? Double
		beginsAt = Date(parcel.readLong())
		type = parcel.readString()
		value = parcel.readString() ?: ""
		label = parcel.readString() ?: ""
		guardianId = parcel.readString() ?: ""
		guardianName = parcel.readString() ?: ""
		site = parcel.readString() ?: ""
		confirmedCount = parcel.readInt()
		rejectedCount = parcel.readInt()
		audioDuration = parcel.readLong()
		
		audioOpusUrl = parcel.readString() ?: ""
		audioPngUrl = parcel.readString() ?: ""
		
		this.windows = RealmList()
		parcel.createTypedArrayList(EventWindow.CREATOR)?.let {
			this.windows!!.addAll(it)
		}
		
		reviewCreated = Date(parcel.readLong())
		reviewConfirmed = when (parcel.readInt()) {
			0 -> false
			1 -> true
			else -> null
		}
	}
	
	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(id)
		parcel.writeString(audioId)
		parcel.writeValue(latitude)
		parcel.writeValue(longitude)
		parcel.writeLong(beginsAt.time)
		parcel.writeString(type)
		parcel.writeString(value)
		parcel.writeString(label)
		parcel.writeString(guardianId)
		parcel.writeString(guardianName)
		parcel.writeString(site)
		parcel.writeValue(confirmedCount)
		parcel.writeValue(rejectedCount)
		parcel.writeValue(audioDuration)
		
		parcel.writeString(audioOpusUrl)
		parcel.writeString(audioPngUrl)
		
		parcel.writeTypedList(windows)
		
		parcel.writeLong(reviewCreated.time)
		parcel.writeInt(when (reviewConfirmed) {
			true -> 1
			false -> 0
			else -> -1
		})
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
		
		// Event value
		const val chainsaw = "chainsaw"
		const val gunshot = "gunshot"
		const val vehicle = "vehicle"
		const val trespasser = "trespasser"
		const val other = "other"
		
	}
}