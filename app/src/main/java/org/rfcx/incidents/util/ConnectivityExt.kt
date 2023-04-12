package org.rfcx.incidents.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

internal fun Context?.isNetworkAvailable(): Boolean {
    if (this == null) return false
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } else {
        val activeNetworkInfo = cm.activeNetworkInfo
        activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }
}
