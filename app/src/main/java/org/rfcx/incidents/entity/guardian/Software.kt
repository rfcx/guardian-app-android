package org.rfcx.incidents.entity.guardian

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class Software(
    @PrimaryKey
    var role: String = "",
    var version: String = "",
    var path: String = ""
) : RealmModel
