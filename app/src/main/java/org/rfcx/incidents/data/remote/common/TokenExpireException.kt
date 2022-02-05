package org.rfcx.incidents.data.remote.common

import android.content.Context
import org.rfcx.incidents.data.preferences.Preferences

class TokenExpireException(context: Context) : Exception() {
    init {
        // clear login pref
        Preferences.getInstance(context).clear()
    }
}
