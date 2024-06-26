package org.rfcx.incidents.util

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import org.rfcx.incidents.service.LocationTrackerService

/**
 * Handle location permission requests and checks
 */

class LocationPermissions(private val activity: Activity) {

    private var onCompletionCallback: ((Boolean) -> Unit)? = null

    fun allowed(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    fun check(onCompletionCallback: (Boolean) -> Unit) {
        this.onCompletionCallback = onCompletionCallback
        if (!allowed()) {
            request()
        } else {
            verifySettings()
        }
    }

    private fun request() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        } else {
            throw Exception("Request permissions not required before API 23 (should never happen)")
        }
    }

    fun handleRequestResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                onCompletionCallback?.invoke(false)
            } else {
                onCompletionCallback?.invoke(true)
            }
        }
    }

    private fun verifySettings() {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(LocationTrackerService.locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(activity)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            onCompletionCallback?.invoke(true)
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed by showing the user a dialog
                try {
                    // Show the dialog and check the result in onActivityResult()
                    exception.startResolutionForResult(activity, REQUEST_CHECK_LOCATION_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                    sendEx.printStackTrace()
                    onCompletionCallback?.invoke(false)
                }
            } else {
                onCompletionCallback?.invoke(false)
            }
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int) {
        if (requestCode == REQUEST_CHECK_LOCATION_SETTINGS) {
            onCompletionCallback?.invoke(resultCode == Activity.RESULT_OK)
        }
    }

    companion object {
        const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
        private const val REQUEST_CHECK_LOCATION_SETTINGS = 35
    }
}
