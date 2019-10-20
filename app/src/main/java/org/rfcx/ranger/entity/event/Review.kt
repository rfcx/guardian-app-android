package org.rfcx.ranger.entity.event

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class Review() : RealmModel, Parcelable {
	
	@PrimaryKey
	@SerializedName("created")
	var created: Int? = null
	@SerializedName("confirmed")
	var confirmed: Boolean? = null
	
	constructor(parcel: Parcel) : this() {
		created = parcel.readValue(Int::class.java.classLoader) as? Int
		confirmed = when (parcel.readInt()) {
			0 -> false
			1 -> true
			else -> null
		}
	}
	
	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeValue(created)
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