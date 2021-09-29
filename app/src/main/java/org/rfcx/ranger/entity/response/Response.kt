package org.rfcx.ranger.entity.response

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.rfcx.ranger.R
import org.rfcx.ranger.data.remote.response.CreateResponseRequest
import org.rfcx.ranger.util.toIsoString
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
		var guardianName: String = "",
		var syncState: Int = SyncState.UNSENT.value
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
		const val RESPONSE_GUARDIAN_NAME = "guardianName"
		const val RESPONSE_SYNC_STATE = "syncState"
	}
}

enum class SyncState(val value: Int) {
	UNSENT(0), SENDING(1), SENT(2)
}

enum class LoggingScale(val value: Int) {
	NOT_SURE(0), SMALL(1), LARGE(2)
}

enum class DamageScale(val value: Int) {
	NO_VISIBLE(0), SMALL(1), MEDIUM(2), LARGE(3)
}

enum class EvidenceTypes(val value: Int) {
	NONE(100),
	CUT_DOWN_TREES(101),
	CLEARED_AREAS(102),
	LOGGING_EQUIPMENT(103),
	LOGGERS_AT_SITE(104),
	ILLEGAL_CAMPS(105),
	FIRED_BURNED_AREAS(106),
	EVIDENCE_OF_POACHING(107)
}

enum class Actions(val value: Int) {
	NONE(200),
	COLLECTED_EVIDENCE(201),
	ISSUE_A_WARNING(202),
	CONFISCATED_EQUIPMENT(203),
	ISSUE_A_FINE(204),
	ARRESTS(205),
	PLANNING_TO_COME_BACK_WITH_SECURITY_ENFORCEMENT(206),
	OTHER(207)
}

fun Response.toCreateResponseRequest(): CreateResponseRequest =
		CreateResponseRequest(
				this.investigatedAt.toIsoString(),
				this.startedAt.toIsoString(),
				this.submittedAt?.toIsoString() ?: "",
				this.evidences,
				this.loggingScale,
				this.damageScale,
				this.responseActions,
				this.note ?: "",
				this.guardianId
		)

fun Response.syncImage() = when (this.syncState) {
	SyncState.UNSENT.value -> R.drawable.ic_cloud_queue
	SyncState.SENDING.value -> R.drawable.ic_cloud_upload
	else -> R.drawable.ic_cloud_done
}

fun Response.syncLabel() = when (this.syncState) {
	SyncState.UNSENT.value -> R.string.unsent
	SyncState.SENDING.value -> R.string.sending
	else -> R.string.sent
}
