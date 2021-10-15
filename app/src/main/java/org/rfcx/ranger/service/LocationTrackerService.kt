package org.rfcx.ranger.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationRequest
import io.realm.Realm
import org.rfcx.ranger.entity.location.Coordinate
import org.rfcx.ranger.entity.location.Tracking
import org.rfcx.ranger.localdb.TrackingDb
import org.rfcx.ranger.util.Analytics
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.RealmHelper
import java.util.*
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
		
		private const val FIVE_MINUTES = 5 * 60 * 1000
		private const val LOCATION_INTERVAL = 1000L * 20L // 20 seconds
		private const val LOCATION_DISTANCE = 0f// 0 meter
		private const val TAG = "LocationTrackerService"
	}
	
	private val realm by lazy { Realm.getInstance(RealmHelper.migrationConfig()) }
	private val trackingDb by lazy { TrackingDb(realm) }
	
	private val binder = LocationTrackerServiceBinder()
	
	private var mLocationManager: LocationManager? = null
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
		override fun onLocationChanged(p0: Location) {
			val myProcess = RunningAppProcessInfo()
			ActivityManager.getMyMemoryState(myProcess)
			val isInForeground = myProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
			val lastGetLocationTime = Preferences.getInstance(this@LocationTrackerService).getLong(Preferences.LATEST_GET_LOCATION_TIME, 0L)
			if (isInForeground && System.currentTimeMillis() - lastGetLocationTime >= FIVE_MINUTES) {
				val time = calculateTime(Calendar.getInstance().time, lastUpdated ?: Date())
				analytics.trackLocationTracking(time)
				saveLocation(p0)
			} else if (myProcess.importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				this@LocationTrackerService.stopService(Intent(this@LocationTrackerService, LocationTrackerService::class.java))
			}
		}
		
		override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
		
		override fun onProviderEnabled(p0: String) {}
		
		override fun onProviderDisabled(p0: String) {}
		
	}
	
	private val gnssStatusCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
		object : GnssStatus.Callback() {
			override fun onSatelliteStatusChanged(status: GnssStatus) {
				super.onSatelliteStatusChanged(status)
				val satCount = status?.satelliteCount ?: 0
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
		val check = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
		if (!check) {
			this.stopSelf()
		}
		mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
		try {
			mLocationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListener)
			
			// Get satellite count
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				if (gnssStatusCallback != null) {
					mLocationManager?.registerGnssStatusCallback(gnssStatusCallback)
				}
			} else {
				mLocationManager?.addGpsStatusListener(gpsStatusListener)
			}
			
			// Tracking last know location timer
			trackingWorkTimer?.cancel()
			
			// Tracking satellite
			trackingSatelliteTimer?.cancel()
			trackingSatelliteTimer = fixedRateTimer("satellite_timer", false, 30 * 1000, 30 * 1000) {
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
	
	private fun saveLocation(location: Location) {
		tracking.id = 1
		val coordinate = Coordinate(
				latitude = location.latitude,
				longitude = location.longitude,
				altitude = location.altitude
		)
		trackingDb.insertOrUpdate(tracking, coordinate)
		LocationSyncWorker.enqueue()
		Preferences.getInstance(this).putLong(Preferences.LATEST_GET_LOCATION_TIME, System.currentTimeMillis())
	}
	
	private fun clearTimer() {
		trackingSatelliteTimer?.cancel()
		trackingWorkTimer?.cancel()
	}
}
