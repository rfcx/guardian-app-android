package org.rfcx.ranger.entity.response

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Response(
		@PrimaryKey
		var id: Int = 0, // local id
		var guid: String? = null,
		var investigatedAt: Date = Date(),
		var startedAt: Date = Date(),
		var submittedAt: Date? = null,
		var evidences: RealmList<Int> = RealmList(),
		var loggingScale: Int = 0,
		var damageScale: Int = 0,
		var responseActions: RealmList<Int> = RealmList(),
		var note: String? = null,
		var guardianId: String = "",
		var syncState: Int = 0
) : RealmObject() {
	companion object {
		const val TABLE_NAME = "Response"
		const val RESPONSE_ID = "id"
		const val RESPONSE_GUID = "guid"
		const val RESPONSE_INVESTIGATED_AT = "investigatedAt"
		const val RESPONSE_STARTED_AT = "startedAt"
		const val RESPONSE_SUBMITTED_AT = "submittedAt"
		const val RESPONSE_EVIDENCES = "evidences"
		const val RESPONSE_LOGGING_SCALE = "loggingScale"
		const val RESPONSE_DAMAGE_SCALE = "damageScale"
		const val RESPONSE_RESPONSE_ACTIONS = "responseActions"
		const val RESPONSE_NOTE = "note"
		const val RESPONSE_GUARDIAN_ID = "guardianId"
		const val RESPONSE_SYNC_STATE = "syncState"
	}
}
