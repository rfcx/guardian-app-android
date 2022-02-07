package org.rfcx.incidents.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.GnssStatus
import android.location.GpsStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationRequest
import io.realm.Realm
import org.rfcx.incidents.R
import org.rfcx.incidents.data.local.TrackingDb
import org.rfcx.incidents.data.local.realm.AppRealm
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.entity.location.Coordinate
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.util.Analytics
import org.rfcx.incidents.view.MainActivity
import java.util.Date
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer

/**
 *
 * Service work with location
 * Save Ranger location to local db.
 */
class LocationTrackerService : Service() {

    companion object {
        val locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        const val NOTIFICATION_LOCATION_ID = 22
        const val NOTIFICATION_LOCATION_NAME = "Track Ranger location"
        const val NOTIFICATION_LOCATION_CHANNEL_ID = "Location"

        private const val FIVE_MINUTES = 5L * 60L * 1000L
        private const val LOCATION_INTERVAL = 1000L * 20L // 20 seconds
        private const val LOCATION_DISTANCE = 0f // 0 meter
        private const val TAG = "LocationTrackerService"
    }

    private val realm by lazy { Realm.getInstance(AppRealm.configuration()) }
    private val trackingDb by lazy { TrackingDb(realm) }

    private val binder = LocationTrackerServiceBinder()

    private var mLocationManager: LocationManager? = null
    private var isLocationAvailability: Boolean = true
    private var trackingStatTimer: Timer? = null
    private var trackingWorkTimer: Timer? = null
    private var trackingSatelliteTimer: Timer? = null
    var lastUpdated: Date? = null
    private val analytics by lazy { Analytics(this) }
    private var satelliteCount = 0
    private var tracking = Tracking()

    fun calculateTime(newTime: Date, lastTime: Date): Long {
        val differenceTime1 = newTime.time - lastTime.time
        lastUpdated = newTime
        return TimeUnit.MILLISECONDS.toSeconds(differenceTime1)
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val myProcess = RunningAppProcessInfo()
            ActivityManager.getMyMemoryState(myProcess)
            val isInForeground = myProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            val lastGetLocationTime =
                Preferences.getInstance(this@LocationTrackerService).getLong(Preferences.LATEST_GET_LOCATION_TIME, 0L)
            if (isInForeground && System.currentTimeMillis() - lastGetLocationTime >= FIVE_MINUTES) {
                saveLocation(location)
            } else if (myProcess.importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                this@LocationTrackerService.stopService(
                    Intent(
                        this@LocationTrackerService,
                        LocationTrackerService::class.java
                    )
                )
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
                getNotificationManager().notify(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(true))
            } else if (status == LocationProvider.OUT_OF_SERVICE) {
                getNotificationManager().notify(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(false))
            }
        }

        override fun onProviderEnabled(provider: String) {
            getNotificationManager().notify(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(true))
        }

        override fun onProviderDisabled(provider: String) {
            getNotificationManager().notify(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(false))
        }
    }

    private val gnssStatusCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                super.onSatelliteStatusChanged(status)
                val satCount = status.satelliteCount
                satelliteCount = satCount
            }
        }
    } else {
        null
    }

    @Deprecated("For old version")
    @SuppressLint("MissingPermission")
    private val gpsStatusListener = GpsStatus.Listener { event ->
        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            var satCount: Int
            try {
                val status = mLocationManager?.getGpsStatus(null)
                val sat = status?.satellites?.iterator()
                satCount = 0
                if (sat != null) {
                    while (sat.hasNext()) {
                        satCount++
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                satCount = 0 // set min of satellite?
            }
            satelliteCount = satCount
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        // check login first
        if (Preferences.getInstance(this).getString(Preferences.ID_TOKEN, "").isNotEmpty()) {
            startTracker()
        }
    }

    private fun startTracker() {
        val check = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!check) {
            this.stopSelf()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        try {
            mLocationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_INTERVAL,
                LOCATION_DISTANCE,
                locationListener
            )

            // Get satellite count
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (gnssStatusCallback != null) {
                    mLocationManager?.registerGnssStatusCallback(gnssStatusCallback)
                }
            } else {
                mLocationManager?.addGpsStatusListener(gpsStatusListener)
            }

            startForeground(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(true))

            trackingStatTimer?.cancel()
            trackingStatTimer = fixedRateTimer("timer", false, FIVE_MINUTES, FIVE_MINUTES) {
                getNotificationManager().notify(
                    NOTIFICATION_LOCATION_ID,
                    createLocationTrackerNotification(isLocationAvailability)
                )
            }

            // Tracking last know location timer
            trackingWorkTimer?.cancel()

            // Tracking satellite
            trackingSatelliteTimer?.cancel()
            trackingSatelliteTimer = fixedRateTimer("satellite_timer", false, FIVE_MINUTES, FIVE_MINUTES) {
                analytics.trackSatelliteCount(satelliteCount) // tracking satellite count per 30s
            }
        } catch (ex: SecurityException) {
            ex.printStackTrace()
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mLocationManager?.removeUpdates(locationListener)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (gnssStatusCallback != null) {
                    mLocationManager?.unregisterGnssStatusCallback(gnssStatusCallback)
                }
            } else {
                mLocationManager?.removeGpsStatusListener(gpsStatusListener)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // set end time of tracking service
        clearTimer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    inner class LocationTrackerServiceBinder : Binder() {
        val trackerService: LocationTrackerService
            get() = this@LocationTrackerService
    }

    private fun createLocationTrackerNotification(isLocationAvailability: Boolean): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, NOTIFICATION_LOCATION_CHANNEL_ID).apply {
            setContentTitle(getString(R.string.notification_tracking_title))
            this@LocationTrackerService.isLocationAvailability = isLocationAvailability
            setSmallIcon(R.drawable.ic_notification)
            setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_large_tracking_location_img))
            setOnlyAlertOnce(true)
            setContentIntent(pendingIntent)
            priority = NotificationCompat.PRIORITY_HIGH
        }.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_LOCATION_CHANNEL_ID,
            NOTIFICATION_LOCATION_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(false)
            enableLights(false)
            setSound(null, null)
            setShowBadge(false)
        }
        getNotificationManager().createNotificationChannel(channel)
    }

    private fun getNotificationManager(): NotificationManager {
        return getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun saveLocation(location: Location) {
        tracking.id = 1
        val coordinate = Coordinate(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude
        )
        trackingDb.insertOrUpdate(tracking, coordinate)
        Preferences.getInstance(this).putLong(Preferences.LATEST_GET_LOCATION_TIME, System.currentTimeMillis())
    }

    private fun clearTimer() {
        trackingStatTimer?.cancel()
        trackingSatelliteTimer?.cancel()
        trackingWorkTimer?.cancel()
    }
}
