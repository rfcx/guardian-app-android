package org.rfcx.incidents.entity.response

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.rfcx.incidents.R
import org.rfcx.incidents.data.remote.response.CreateResponseRequest
import org.rfcx.incidents.util.toIsoString
import java.util.Date

open class Response(
    @PrimaryKey
    var id: Int = 0, // local id
    var guid: String? = null,
    var investigatedAt: Date = Date(),
    var startedAt: Date = Date(),
    var submittedAt: Date? = null,
    var answers: RealmList<Int> = RealmList(),
    var evidences: RealmList<Int> = RealmList(),
    var loggingScale: Int = LoggingScale.DEFAULT.value,
    var damageScale: Int = DamageScale.DEFAULT.value,
    var responseActions: RealmList<Int> = RealmList(),
    var investigateType: RealmList<Int> = RealmList(),
    var poachingScale: Int = PoachingScale.DEFAULT.value,
    var poachingEvidence: RealmList<Int> = RealmList(),
    var note: String? = null,
    var streamId: String = "",
    var streamName: String = "",
    var audioLocation: String? = null,
    var incidentRef: String? = null,
    var assets: RealmList<Asset> = RealmList(),
    var syncState: Int = SyncState.UNSENT.value
) : RealmObject() {
    val imageAssets: List<Asset> get() = this.assets.filter { a -> a.type == AssetType.IMAGE }
    val audioAssets: List<Asset> get() = this.assets.filter { a -> a.type == AssetType.AUDIO }
    val trackingAssets: List<Asset> get() = this.assets.filter { a -> a.type == AssetType.KML }

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
        const val RESPONSE_STREAM_ID = "streamId"
        const val RESPONSE_STREAM_NAME = "streamName"
        const val RESPONSE_AUDIO_LOCATION = "audioLocation"
        const val RESPONSE_INCIDENT_REF = "incidentRef"
        const val RESPONSE_SYNC_STATE = "syncState"
        const val RESPONSE_INVESTIGATE_TYPE = "investigateType"
        const val RESPONSE_POACHING_SCALE = "poachingScale"
        const val RESPONSE_POACHING_EVIDENCE = "poachingEvidence"
        const val RESPONSE_ANSWERS = "answers"
        const val RESPONSE_ASSETS = "assets"
    }
}

enum class SyncState(val value: Int) {
    UNSENT(0), SENDING(1), SENT(2)
}

enum class InvestigationType(val value: Int) {
    DEFAULT(-1), LOGGING(501), POACHING(502), OTHER(503)
}

enum class LoggingScale(val value: Int) {
    DEFAULT(-1), NONE(301), SMALL(302), LARGE(303)
}

enum class PoachingScale(val value: Int) {
    DEFAULT(-1), SMALL(701), LARGE(702), NONE(703)
}

enum class DamageScale(val value: Int) {
    DEFAULT(-1), NO_VISIBLE(401), SMALL(402), MEDIUM(403), LARGE(404)
}

enum class PoachingEvidence(val value: Int) {
    DEFAULT(-1), BULLET_SHELLS(601), FOOTPRINTS(602), DOG_TRACKS(603), OTHER(604), NONE(605)
}

enum class EvidenceTypes(val value: Int) {
    NONE(100),
    CUT_DOWN_TREES(101),
    CLEARED_AREAS(102),
    LOGGING_EQUIPMENT(103),
    LOGGERS_AT_SITE(104),
    ILLEGAL_CAMPS(105),
    FIRED_BURNED_AREAS(106),
    OTHER(108)
}

enum class Actions(val value: Int) {
    NONE(200),
    COLLECTED_EVIDENCE(201),
    ISSUE_A_WARNING(202),
    CONFISCATED_EQUIPMENT(203),
    ISSUE_A_FINE(204),
    ARRESTS(205),
    PLANNING_TO_COME_BACK_WITH_SECURITY_ENFORCEMENT(206),
    OTHER(207),
    DAMAGED_MACHINERY(208)
}

fun Response.toCreateResponseRequest(): CreateResponseRequest =
    CreateResponseRequest(
        this.investigatedAt.toIsoString(),
        this.startedAt.toIsoString(),
        this.submittedAt?.toIsoString() ?: "",
        this.answers,
        this.note,
        this.streamId
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

fun Response.saveToAnswers(): RealmList<Int> {
    val answers = RealmList<Int>()
    answers.addAll(this.responseActions)
    answers.addAll(this.investigateType)

    if (this.investigateType.contains(InvestigationType.POACHING.value)) {
        answers.addAll(this.poachingEvidence)
        answers.add(this.poachingScale)
    }

    if (this.investigateType.contains(InvestigationType.LOGGING.value)) {
        answers.addAll(this.evidences)
        answers.add(this.loggingScale)
    }
    return answers
}
