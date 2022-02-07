package org.rfcx.incidents.entity.event

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import org.rfcx.incidents.entity.stream.Incident
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
