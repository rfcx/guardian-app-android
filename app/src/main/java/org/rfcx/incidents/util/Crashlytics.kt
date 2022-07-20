package org.rfcx.incidents.util

import android.app.Activity
import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.rfcx.incidents.entity.CrashlyticsKey

class Crashlytics() {
    private val firebaseCrashlytics = FirebaseCrashlytics.getInstance()

    fun setCustomKey(key: String, value: String) {
        firebaseCrashlytics.setCustomKey(key, value)
    }
}
