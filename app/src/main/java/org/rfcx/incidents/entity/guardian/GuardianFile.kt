package org.rfcx.incidents.entity.guardian

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class GuardianFile(
    @PrimaryKey
    var id: String = "",
    var name: String = "",
    var version: String = "",
    var path: String = "",
    var type: String = "",
    var url: String = "",
    var meta: String = ""
) : RealmModel {
    companion object {
        const val TABLE = "GuardianFile"
        const val FIELD_ID = "id"
        const val FIELD_NAME = "name"
        const val FIELD_VERSION = "version"
        const val FIELD_PATH = "path"
        const val FIELD_TYPE = "type"
        const val FIELD_URL = "url"
        const val FIELD_META = "meta"
    }
}

enum class GuardianFileType(val value: String) {
    SOFTWARE("software"), CLASSIFIER("classifier")
}
