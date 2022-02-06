package org.rfcx.incidents.entity.event

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import org.rfcx.incidents.entity.Incident
import java.util.Date

@RealmClass
open class Alert(
    @PrimaryKey
    var id: Int = 0,
    var serverId: String = "",
    var name: String = "",
    var streamId: String = "",
    var projectId: String = "",
    var createdAt: Date = Date(),
    var start: Date = Date(),
    var end: Date = Date(),
    var classification: Classification? = null,
    var incident: Incident? = null
) : RealmModel {
    companion object {
        const val TABLE_NAME = "Alert"
        const val ALERT_ID = "id"
        const val ALERT_NAME = "name"
        const val ALERT_SERVER_ID = "serverId"
        const val ALERT_STREAM_ID = "streamId"
        const val ALERT_PROJECT_ID = "projectId"
        const val ALERT_CREATED_AT = "createdAt"
        const val ALERT_START = "start"
        const val ALERT_END = "end"
        const val ALERT_CLASSIFICATION = "classification"
        const val ALERT_INCIDENT = "incident"
    }
}
