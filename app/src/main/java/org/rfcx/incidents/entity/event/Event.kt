package org.rfcx.incidents.entity.event

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import org.rfcx.incidents.R
import org.rfcx.incidents.entity.stream.Incident
import org.rfcx.incidents.view.events.adapter.StreamAdapter
import java.util.Date

@RealmClass
open class Event(
    @PrimaryKey
    var id: String = "",
    var name: String = "",
    var streamId: String = "",
    var createdAt: Date = Date(),
    var start: Date = Date(),
    var end: Date = Date(),
    var classification: Classification? = null,
    var incident: Incident? = null
) : RealmModel {

    val valueIcon get() = when (this.classification?.value) {
        StreamAdapter.GUNSHOT -> R.drawable.ic_gun
        StreamAdapter.CHAINSAW -> R.drawable.ic_chainsaw
        StreamAdapter.VEHICLE -> R.drawable.ic_vehicle
        StreamAdapter.VOICE -> R.drawable.ic_voice
        StreamAdapter.DOG_BARK -> R.drawable.ic_dog_bark
        StreamAdapter.ELEPHANT -> R.drawable.ic_elephant
        else -> R.drawable.ic_report
    }

    val valueTitle: Int? get() = when (this.classification?.value) {
        StreamAdapter.GUNSHOT -> R.string.gunshot
        StreamAdapter.CHAINSAW -> R.string.chainsaw
        StreamAdapter.VEHICLE -> R.string.vehicle
        StreamAdapter.VOICE -> R.string.human_voice
        StreamAdapter.DOG_BARK -> R.string.dog_bark
        StreamAdapter.ELEPHANT -> R.string.elephant
        else -> null
    }

    companion object {
        const val TABLE_NAME = "Event"
        const val EVENT_ID = "id"
        const val EVENT_NAME = "name"
        const val EVENT_STREAM_ID = "streamId"
        const val EVENT_PROJECT_ID = "projectId"
        const val EVENT_CREATED_AT = "createdAt"
        const val EVENT_START = "start"
        const val EVENT_END = "end"
        const val EVENT_CLASSIFICATION = "classification"
        const val EVENT_INCIDENT = "incident"
    }
}
