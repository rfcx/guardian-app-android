package org.rfcx.incidents.util

import android.content.Context

class ConnectivityUtils(private val context: Context) {

    fun isAvailable(): Boolean {
        return context.isNetworkAvailable()
    }
}
