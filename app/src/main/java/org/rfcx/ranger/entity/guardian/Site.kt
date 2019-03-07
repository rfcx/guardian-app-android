package org.rfcx.ranger.entity.guardian

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * A partner or place containing guardians
 */

open class Site : RealmObject() {
    @PrimaryKey
    @SerializedName("guid")
    var id: String = ""
    var name: String = ""
    var description: String = ""
}
