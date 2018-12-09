package org.rfcx.ranger.entity.message

import io.realm.RealmObject

open class To : RealmObject(){

    var guid: String = ""
    var email: String = ""
    var firstname: String? = ""
    var lastname: String? = ""
//    var username: Any? = null
//    var accessibleSites: List<Any>? = null
//    var defaultSite: Any? = null
}