package org.rfcx.incidents.util

import android.content.Context
import android.net.ConnectivityManager

internal fun Context?.isNetworkAvailable(): Boolean {
    if (this == null) return false
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = cm.activeNetworkInfo
    if (activeNetwork != null) {
        return activeNetwork.isConnected
    }
    return false
}
