package org.rfcx.incidents.entity

import io.realm.RealmModel
import io.realm.annotations.RealmClass

@RealmClass
open class Classification(
    var value: String = "",
    var title: String = ""
) : RealmModel {
    companion object {
        const val TABLE_NAME = "Classification"
        const val CLASSIFICATION_VALUE = "value"
        const val CLASSIFICATION_TITLE = "title"
    }
}
