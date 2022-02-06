package org.rfcx.incidents.entity.stream

import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.RealmClass
import org.rfcx.incidents.entity.event.Event
import java.util.Date

@RealmClass
open class Incident(
    var id: String = "",
    var ref: String = "",
    var closedAt: Date? = null,
    var createdAt: Date = Date(),
    var events: RealmList<Event>? = null
) : RealmModel {
    companion object {
        const val TABLE_NAME = "Incident"
    }
}
