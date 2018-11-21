package org.rfcx.ranger.entity.location

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.rfcx.ranger.util.DateHelper

open class RangerLocation(
		@PrimaryKey
		var time: String = DateHelper.getIsoTime(),
		var latitude: Double = 0.0,
		var longitude: Double = 0.0
) : RealmObject()