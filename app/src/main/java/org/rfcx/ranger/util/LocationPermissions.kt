package org.rfcx.ranger.util

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import org.rfcx.ranger.R
import org.rfcx.ranger.service.LocationTrackerService

/**
 * Handle location permission requests and checks
 */

class LocationPermissions(val activity: Activity) {

    fun handlePermissionsResult(grantResults: IntArray, callback: (Boolean) -> Unit) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            callback(true)
        } else {
            val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)
            if (!shouldProvideRationale) {
                val dialogBuilder: AlertDialog.Builder =
                        AlertDialog.Builder(activity).apply {
                            setTitle(null)
                            setMessage(R.string.location_permission_msg)
                            setPositiveButton(R.string.go_to_setting) { _, _ ->
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:${activity.packageName}"))
                                intent.addCategory(Intent.CATEGORY_DEFAULT)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                activity.startActivity(intent)
                            }
                        }
                dialogBuilder.create().show()
            }
            LocationTracking.set(activity, false)
            callback(false)
        }
    }

    fun check(requestIdentifier: Int, callback: (Boolean) -> Unit) {
        val builder = LocationSettingsRequest.Builder().addLocationRequest(LocationTrackerService.locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(activity)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            LocationTracking.set(activity, true)
            callback(true)
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                LocationTracking.set(activity, false)
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(activity, requestIdentifier)
                    callback(false)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                    sendEx.printStackTrace()
                    callback(true)
                }
            }
            else {
                callback(true)
            }
        }
    }
}