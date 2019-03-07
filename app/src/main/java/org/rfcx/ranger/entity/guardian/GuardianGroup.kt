package org.rfcx.ranger.entity.guardian

import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * A group of guardians for subscribing notifications
 */

open class GuardianGroup : RealmObject() {
    @PrimaryKey
    var shortname: String = ""
    var name: String = ""
    var description: String = ""
    var guardians: RealmList<Guardian>? = null
    @SerializedName("site")
    var siteId: String = ""
}
