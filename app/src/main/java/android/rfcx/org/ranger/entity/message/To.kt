package android.rfcx.org.ranger.entity.message

import io.realm.RealmObject

/**
 * Created by Jingjoeh on 10/21/2017 AD.
 */
open class To : RealmObject(){

    var guid: String = ""
    var email: String = ""
    var firstname: String = ""
    var lastname: String = ""
//    var username: Any? = null
//    var accessibleSites: List<Any>? = null
//    var defaultSite: Any? = null
}