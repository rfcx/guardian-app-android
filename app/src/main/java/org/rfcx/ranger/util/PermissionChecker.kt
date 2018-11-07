package org.rfcx.ranger.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

fun Context.isLocationAllow(): Boolean {
    val permissionState = ActivityCompat.checkSelfPermission(this.applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION)
    return permissionState == PackageManager.PERMISSION_GRANTED
}