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
    var lastIncident: Incident? = null,
    var guardianType: String? = null,
    var order: Int = Int.MAX_VALUE
) : RealmModel {
    companion object {
        const val TABLE_NAME = "Stream"
        const val FIELD_ID = "id"
        const val FIELD_PROJECT_ID = "projectId"
        const val FIELD_ORDER = "order"
        const val TAG_HOT = "hot"
        const val TAG_RECENT = "recent"
    }
}
