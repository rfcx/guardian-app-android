package org.rfcx.ranger.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

fun Context.isRecordingAudioAllowed(): Boolean {
	val permissionState = ActivityCompat.checkSelfPermission(this.applicationContext,
			Manifest.permission.RECORD_AUDIO)
	return permissionState == PackageManager.PERMISSION_GRANTED
}