package org.rfcx.incidents.entity.stream

import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class Stream(
    @PrimaryKey
    var id: String = "",
    var name: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var timezone: String = "",
    var projectId: String = "",
    var tags: RealmList<String>? = null,
    var lastIncident: Incident? = null
) : RealmModel {
    companion object {
        const val TABLE_NAME = "Stream"
        const val STREAM_ID = "id"
        const val STREAM_NAME = "name"
        const val STREAM_LATITUDE = "latitude"
        const val STREAM_LONGITUDE = "longitude"
        const val STREAM_PROJECT_ID = "projectId"
        const val STREAM_INCIDENT = "incident"
    }
}
