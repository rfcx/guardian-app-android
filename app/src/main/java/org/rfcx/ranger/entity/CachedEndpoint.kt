package org.rfcx.ranger.entity

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

@RealmClass
open class CachedEndpoint(
		@PrimaryKey
		var endpoint: String = "",
		var updatedAt: Date = Date()) : RealmModel {
	companion object {
		const val TABEL_NAME = "CachedEndpoint"
		const val FIELD_ENDPOINT = "endpoint"
		const val FIELD_UPDATED_AT = "updatedAt"
	}
}