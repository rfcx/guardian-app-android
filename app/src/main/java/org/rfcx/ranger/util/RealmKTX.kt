package org.rfcx.ranger.util

import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.RealmResults

fun <T : RealmModel> RealmResults<T>.asLiveData() = RealmLiveData(this)
fun <T : RealmObject> T.asLiveData() = LiveDataRealmObject(this)