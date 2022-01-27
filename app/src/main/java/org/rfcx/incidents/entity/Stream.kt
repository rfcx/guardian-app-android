package org.rfcx.incidents.entity

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class Stream(
    @PrimaryKey
    var id: Int = 0,
    var serverId: String = "",
    var name: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var projectServerId: String = "",
    var incidentRef: Int = 0
) : RealmModel {
    companion object {
        const val TABLE_NAME = "Stream"
        const val STREAM_ID = "id"
        const val STREAM_NAME = "name"
        const val STREAM_SERVER_ID = "serverId"
        const val STREAM_LATITUDE = "latitude"
        const val STREAM_LONGITUDE = "longitude"
        const val STREAM_PROJECT_SERVER_ID = "projectServerId"
        const val STREAM_INCIDENT_REF = "incidentRef"
    }
}
