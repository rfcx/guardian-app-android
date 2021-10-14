package org.rfcx.ranger.entity

import io.realm.RealmModel
import io.realm.annotations.RealmClass

@RealmClass
open class LocationGroup(
		var id: String? = null,
		var name: String = ""
) : RealmModel {
	companion object {
		const val TABLE_NAME = "LocationGroup"
		const val LOCATION_GROUP_ID = "id"
		const val LOCATION_GROUP_NAME = "name"
	}
}
