package android.rfcx.org.ranger.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.rfcx.org.ranger.repo.TokenExpireException
import android.rfcx.org.ranger.repo.api.SendLocationApi
import android.rfcx.org.ranger.util.DateHelper
import android.rfcx.org.ranger.util.PrefKey
import android.rfcx.org.ranger.util.PreferenceHelper
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.google.android.gms.location.*


/**
 * Created by Jingjoeh on 10/7/2017 AD.
 */
class SendLocationLocationService : Service() {
    private val binder = SendLocationLocationServiceBinder()
    private val intervalLocationUpdate: Long = 5 * 60 * 1000
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var mLocationNotificationManager: LocationNotificationManager? = null


    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        val check = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!check) {
            this.stopSelf()
        }

        val locationRequest = LocationRequest()
        locationRequest.interval = intervalLocationUpdate
        locationRequest.fastestInterval = intervalLocationUpdate
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?.let {
                    sendLocation(it.lastLocation)
                }
            }
        }

        fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null)
        mLocationNotificationManager = LocationNotificationManager(this)
        mLocationNotificationManager?.startLocationNotification()
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient?.removeLocationUpdates(locationCallback)
        try {
            mLocationNotificationManager?.stopLocationNotification()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }


    private fun sendLocation(location: Location) {
        SendLocationApi().checkIn(this, location.latitude, location.longitude,
                DateHelper.getIsoTime(), object : SendLocationApi.SendLocationCallBack {
            override fun onSuccess() {
                Log.d("sendLocation", "Success")
            }

            override fun onFailed(t: Throwable?, message: String?) {
                Log.w("sendLocation", if (message.isNullOrEmpty()) "Error" else message)
                if (t != null && t is TokenExpireException) {
                    PreferenceHelper.getInstance(this@SendLocationLocationService).remove(PrefKey.LOGIN_RESPONSE)
                    mLocationNotificationManager?.stopLocationNotification()
                    mLocationNotificationManager?.startReLoginNotification()
                }
            }
        })
    }

    inner class SendLocationLocationServiceBinder : Binder() {
        val service: SendLocationLocationService
            get() = this@SendLocationLocationService

    }

}