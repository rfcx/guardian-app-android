package org.rfcx.ranger.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.*
import android.media.RingtoneManager
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationRequest
import io.realm.Realm
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.R
import org.rfcx.ranger.data.local.WeeklySummaryData
import org.rfcx.ranger.entity.location.CheckIn
import org.rfcx.ranger.localdb.LocationDb
import org.rfcx.ranger.util.Analytics
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.RealmHelper
import org.rfcx.ranger.view.MainActivityNew
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
		const val NOTIFICATION_LOCATION_ID = 22
		const val NOTIFICATION_LOCATION_NAME = "Track Ranger location"
		const val NOTIFICATION_LOCATION_CHANNEL_ID = "Location"
		val locationRequest = LocationRequest().apply {
			priority = LocationRequest.PRIORITY_HIGH_ACCURACY
		}
		
		private const val LOCATION_INTERVAL = 1000L * 20L // 20 seconds
		private const val LOCATION_DISTANCE = 0f// 0 meter
		private const val LASTEST_GET_LOCATION_TIME = "LASTEST_GET_LOCATION_TIME"
		private const val TAG = " LocationTrackerService"
	}
	
	private val binder = LocationTrackerServiceBinder()
	
	private var mLocationManager: LocationManager? = null
	private var isLocationAvailability: Boolean = true
	private var trackingStatTimer: Timer? = null
	private lateinit var weeklySummaryData: WeeklySummaryData
	var lastUpdated: Date? = null
	private val analytics by lazy { Analytics(this) }
	private var satelliteCount = 0
	
	private val delayTime = 1000L * 30L // 30 seconds
	private var satelliteHandler: Handler? = null
	private val satelliteRunnable = object : Runnable {
		override fun run() {
			analytics.trackSatelliteCount(satelliteCount)
			satelliteHandler?.postDelayed(this, delayTime)
		}
	}
	
	fun calculateTime(newTime: Date, lastTime: Date): Long {
		val differenceTime1 = newTime.time - lastTime.time
		lastUpdated = newTime
		return TimeUnit.MILLISECONDS.toSeconds(differenceTime1)
	}
	
	private val locationListener = object : LocationListener {
		
		override fun onLocationChanged(p0: Location?) {
			p0?.let {
				Log.i(TAG, "${it.longitude} , ${it.longitude}")
				val time = calculateTime(Calendar.getInstance().time, lastUpdated ?: Date())
				analytics.trackLocationTracking(time)
				saveLocation(it)
				if (BuildConfig.DEBUG) playSound()
			}
		}
		
		override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
			Log.d(TAG, "onStatusChanged $p0 $p1")
			
			if (p1 == LocationProvider.TEMPORARILY_UNAVAILABLE) {
				if ((System.currentTimeMillis() - Preferences.getInstance(this@LocationTrackerService).getLong(LASTEST_GET_LOCATION_TIME, 0L)) > 10 * 1000L) {
					getNotificationManager().notify(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(true))
				}
				
			} else if (p1 == LocationProvider.OUT_OF_SERVICE) {
				getNotificationManager().notify(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(false))
			}
		}
		
		override fun onProviderEnabled(p0: String?) {
			Log.d(TAG, "onProviderEnabled $p0")
			getNotificationManager().notify(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(true))
		}
		
		override fun onProviderDisabled(p0: String?) {
			Log.d(TAG, "onProviderDisabled $p0")
			getNotificationManager().notify(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(false))
		}
		
	}
	
	override fun onBind(p0: Intent?): IBinder? {
		return binder
	}
	
	override fun onCreate() {
		super.onCreate()
		// check login first
		weeklySummaryData = WeeklySummaryData(Preferences(this))
		if (Preferences.getInstance(this).getString(Preferences.ID_TOKEN, "").isNotEmpty()) {
			startTracker()
		}
	}
	
	private fun startTracker() {
		val check = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
		if (!check) {
			this.stopSelf()
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createNotificationChannel()
		}
		mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
		try {
			mLocationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListener)
			
			// Get satellite count
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				mLocationManager?.registerGnssStatusCallback(object : GnssStatus.Callback() {
					override fun onSatelliteStatusChanged(status: GnssStatus?) {
						super.onSatelliteStatusChanged(status)
						val satCount = status?.satelliteCount ?: 0
						Log.i(TAG, "satellite count = $satCount")
						satelliteCount = satCount
					}
				})
			} else {
				mLocationManager?.addGpsStatusListener { event ->
					if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
						var satCount : Int
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
						Log.i(TAG, "satellite count = $satCount")
						satelliteCount = satCount
					}
				}
			}
			
			// Start notification on duty tracking
			startForeground(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(true))
			
			// Tracking stat timer
			trackingStatTimer?.cancel()
			trackingStatTimer = fixedRateTimer("timer", false, 60 * 1000, 60 * 1000) {
				getNotificationManager().notify(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(isLocationAvailability))
			}
			
			// Start handle run
			satelliteHandler = Handler()
			satelliteHandler?.postDelayed(satelliteRunnable, delayTime)
		} catch (ex: SecurityException) {
			ex.printStackTrace()
			Log.w(TAG, "fail to request location update, ignore", ex)
		} catch (ex: IllegalArgumentException) {
			ex.printStackTrace()
			Log.w(TAG, "gps provider does not exist " + ex.message)
		}
		
	}
	
	override fun onDestroy() {
		super.onDestroy()
		Log.e(TAG, "onDestroy")
		clearSatelliteHandler()
		mLocationManager?.removeUpdates(locationListener)
		trackingStatTimer?.cancel()
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
		val intent = Intent(this, MainActivityNew::class.java)
		val pendingIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT)
		return NotificationCompat.Builder(this, NOTIFICATION_LOCATION_CHANNEL_ID).apply {
			setContentTitle(getString(R.string.notification_tracking_title))
			this@LocationTrackerService.isLocationAvailability = isLocationAvailability
			
			if (this@LocationTrackerService.isLocationAvailability) {
				setContentText(getString(R.string.notification_traking_message_format, weeklySummaryData.getOnDutyTimeMinute()))
			} else {
				setContentText(getString(R.string.notification_location_not_availability))
			}
			
			setSmallIcon(R.drawable.ic_notification)
			setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_large_tracking_location_img))
			setOnlyAlertOnce(true)
			setContentIntent(pendingIntent)
			priority = NotificationCompat.PRIORITY_HIGH
		}.build()
	}
	
	@RequiresApi(Build.VERSION_CODES.O)
	private fun createNotificationChannel() {
		val channel = NotificationChannel(NOTIFICATION_LOCATION_CHANNEL_ID, NOTIFICATION_LOCATION_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
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
		LocationDb(Realm.getInstance(RealmHelper.migrationConfig())).save(CheckIn(latitude = location.latitude, longitude = location.longitude))
		LocationSyncWorker.enqueue()
		Preferences.getInstance(this).putLong(LASTEST_GET_LOCATION_TIME, System.currentTimeMillis())
	}
	
	// Just for debug mode
	private fun playSound() {
		try {
			val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
			val r = RingtoneManager.getRingtone(this, notification)
			r.play()
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}
	
	private fun clearSatelliteHandler() {
		satelliteHandler?.removeCallbacks(satelliteRunnable)
		satelliteHandler = null
	}
}