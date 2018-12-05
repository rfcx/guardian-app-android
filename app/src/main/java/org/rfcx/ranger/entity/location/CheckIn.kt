package org.rfcx.ranger.entity.location

import com.google.gson.annotations.Expose
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.rfcx.ranger.util.DateHelper

open class CheckIn(
		var time: String = DateHelper.getIsoTime(),
		var latitude: Double = 0.0,
		var longitude: Double = 0.0,
		@PrimaryKey
		@Expose(serialize = false)
		var id: Int = 0,
		@Expose(serialize = false)
		var timestamp: Long = System.currentTimeMillis(),
		@Expose(serialize = false)
		var synced: Boolean = false
) : RealmObject()