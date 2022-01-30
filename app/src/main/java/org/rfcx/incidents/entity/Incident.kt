package org.rfcx.incidents.entity

import io.realm.RealmModel
import io.realm.annotations.RealmClass
import java.util.*


@RealmClass
open class Incident(
    var id: String = "",
    var closedAt: Date? = null,
    var createdAt: Date = Date()
) : RealmModel {
    companion object {
        const val TABLE_NAME = "Incident"
        const val INCIDENT_ID = "id"
        const val INCIDENT_CLOSED_AT = "name"
        const val INCIDENT_CREATED_AT = "serverId"
    }
}
