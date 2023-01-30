package org.rfcx.incidents.entity.guardian

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class GuardianFile(
    @PrimaryKey
    var role: String = "",
    var version: String = "",
    var path: String = "",
    var type: String = "",
    var meta: String = ""
) : RealmModel

enum class GuardianFileType(val value: String) {
    SOFTWARE("software"), CLASSIFIER("classifier")
}
