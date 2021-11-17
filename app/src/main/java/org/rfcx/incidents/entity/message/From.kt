package org.rfcx.incidents.entity.message

import io.realm.RealmObject

open class From : RealmObject() {
    var guid: String = ""
    var email: String = ""
    var firstname: String? = ""
    var lastname: String? = ""
//    var username: Any? = null
//    var accessibleSites: List<Any>? = null
//    var defaultSite: Any? = null
}
