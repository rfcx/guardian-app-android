package org.rfcx.incidents.entity.common

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.Date

@RealmClass
open class CachedEndpoint(
    @PrimaryKey
    var endpoint: String = "",
    var updatedAt: Date = Date()
) : RealmModel {
    companion object {
        const val TABLE_NAME = "CachedEndpoint"
        const val FIELD_ENDPOINT = "endpoint"
        const val FIELD_UPDATED_AT = "updatedAt"
    }
}
