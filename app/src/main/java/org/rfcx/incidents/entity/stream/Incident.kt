package org.rfcx.incidents.entity.stream

import io.realm.RealmList
import io.realm.RealmModel
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.RealmClass
import org.rfcx.incidents.entity.event.Event
import java.util.Date

@RealmClass
open class Incident(
    var id: String = "",
    var ref: String = "",
    var closedAt: Date? = null,
    var createdAt: Date = Date(),
    @LinkingObjects("incident") val events: RealmResults<Event>? = null,
    val responses: RealmList<CreatedBy>? = null,
) : RealmModel {
    companion object {
        const val TABLE_NAME = "Incident"
        const val FIELD_ID = "id"
        const val FIELD_RESPONSES = "responses"
    }
}
