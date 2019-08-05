package org.rfcx.ranger.util

import io.realm.RealmModel
import io.realm.RealmResults

fun <T: RealmModel> RealmResults<T>.asLiveData() = RealmLiveData(this)