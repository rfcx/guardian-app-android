package android.rfcx.org.ranger.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.rfcx.org.ranger.entity.RangerLocation
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.google.android.gms.location.*
import io.realm.Realm


/**
 * Created by Jingjoeh on 10/7/2017 AD.
 */
class SaveLocationService : Service() {
    private val tag = SaveLocationService::class.java.simpleName
    private val binder = SendLocationLocationServiceBinder()
    private val intervalLocationUpdate: Long = 10 * 1000
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "onCreate")
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
                Log.d(tag, "${locationResult?.lastLocation?.latitude} ${locationResult?.lastLocation?.longitude}")
                locationResult?.let {
                    val realm = Realm.getDefaultInstance()
                    realm.beginTransaction()
                    realm.copyToRealm(RangerLocation().apply {
                        latitude = it.lastLocation.latitude
                        longitude = it.lastLocation.longitude
                    })
                    realm.commitTransaction()
                    realm.close()

                }
            }
        }

        fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("LocationLocationService", "onDestroy")
        locationCallback?.let { fusedLocationClient?.removeLocationUpdates(it) }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    inner class SendLocationLocationServiceBinder : Binder() {
        val service: SaveLocationService
            get() = this@SaveLocationService

    }

}