package org.rfcx.ranger.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

/**
 * Created by Jingjoeh on 12/16/2017 AD.
 */

fun Context.isLocationAllow(): Boolean {
    val permissionState = ActivityCompat.checkSelfPermission(this.applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION)
    return permissionState == PackageManager.PERMISSION_GRANTED
}