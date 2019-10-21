package org.rfcx.ranger.entity.event

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.annotations.RealmClass
import java.util.*

@RealmClass
open class Review() : RealmModel, Parcelable {
	
	@SerializedName("created")
	var created: Date = Date()
	@SerializedName("confirmed")
	var confirmed: Boolean? = null
	
	constructor(parcel: Parcel) : this() {
		created = Date(parcel.readLong())
		confirmed = when (parcel.readInt()) {
			0 -> false
			1 -> true
			else -> null
		}
	}
	
	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeLong(created.time)
		parcel.writeInt(when (confirmed) {
			true -> 1
			false -> 0
			else -> -1
		})
	}
	
	override fun describeContents(): Int {
		return 0
	}
	
	companion object CREATOR : Parcelable.Creator<Review> {
		override fun createFromParcel(parcel: Parcel): Review {
			return Review(parcel)
		}
		
		override fun newArray(size: Int): Array<Review?> {
			return arrayOfNulls(size)
		}
	}
}