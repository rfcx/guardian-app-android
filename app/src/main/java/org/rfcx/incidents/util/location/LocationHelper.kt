package org.rfcx.incidents.util.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.rfcx.incidents.view.guardian.checklist.site.GuardianSiteSetFragment
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationHelper(private val context: Context) {
    companion object {
        const val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
        const val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
    }
    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationEngine: LocationEngine = LocationEngineProvider.getBestLocationEngine(context)

    @SuppressLint("MissingPermission")
    fun getFlowLocationChanged(): Flow<Location?> {
        return callbackFlow {
            val callback = object : LocationEngineCallback<LocationEngineResult> {
                override fun onSuccess(result: LocationEngineResult?) {
                    trySendBlocking(result?.lastLocation)
                }

                override fun onFailure(exception: Exception) {
                    // do nothing only allow correct location record
                }
            }
            if (hasLocationPermission()) {
                val request =
                    LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                        .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                        .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build()
                locationEngine.requestLocationUpdates(
                    request,
                    callback,
                    Looper.getMainLooper()
                )
                locationEngine.getLastLocation(callback)
            }
            awaitClose {
                locationEngine.removeLocationUpdates(callback)
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getLastLocation(): Location? {
        return suspendCoroutine { cont ->
            if (hasLocationPermission()) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    cont.resume(location)
                }
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
}
