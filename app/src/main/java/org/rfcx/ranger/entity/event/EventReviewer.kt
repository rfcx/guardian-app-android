package org.rfcx.ranger.entity.event

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

@RealmClass
open class EventReviewer() : RealmModel, Parcelable {
	
	@PrimaryKey
	@SerializedName("guid")
	var guid: String = ""
	@SerializedName("firstName")
	var firstName: String? = ""
	@SerializedName("lastName")
	var lastName: String? = ""
	@SerializedName("createdAt")
	var createdAt: Date = Date()
	@SerializedName("lastLogin")
	var lastLogin: Date = Date()
	@SerializedName("freezeUsername")
	var freezeUsername: Boolean? = null
	@SerializedName("pictureUrl")
	var pictureUrl: String? = ""
	@SerializedName("locale")
	var locale: String? = ""
	@SerializedName("email")
	var email: String? = ""
	@SerializedName("updatedAt")
	var updatedAt: Date = Date()
	@SerializedName("username")
	var username: String? = ""
	
	constructor(parcel: Parcel) : this() {
		firstName = parcel.readString()
		lastName = parcel.readString()
		createdAt = Date(parcel.readLong())
		lastLogin = Date(parcel.readLong())
		freezeUsername = when (parcel.readInt()) {
			0 -> false
			1 -> true
			else -> null
		}
		pictureUrl = parcel.readString()
		guid = parcel.readString().toString()
		locale = parcel.readString()
		email = parcel.readString()
		updatedAt = Date(parcel.readLong())
		username = parcel.readString()
	}
	
	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(firstName)
		parcel.writeString(lastName)
		parcel.writeLong(createdAt.time)
		parcel.writeLong(lastLogin.time)
		parcel.writeInt(when (freezeUsername) {
			true -> 1
			false -> 0
			else -> -1
		})
		parcel.writeString(pictureUrl)
		parcel.writeString(guid)
		parcel.writeString(locale)
		parcel.writeString(email)
		parcel.writeLong(updatedAt.time)
		parcel.writeString(username)
	}
	
	override fun describeContents(): Int {
		return 0
	}
	
	companion object CREATOR : Parcelable.Creator<EventReviewer> {
		override fun createFromParcel(parcel: Parcel): EventReviewer {
			return EventReviewer(parcel)
		}
		
		override fun newArray(size: Int): Array<EventReviewer?> {
			return arrayOfNulls(size)
		}
	}
}
