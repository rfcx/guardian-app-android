package org.rfcx.ranger.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.location.LocationProvider
import android.media.RingtoneManager
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationRequest
import org.rfcx.ranger.BuildConfig
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.location.RangerLocation
import org.rfcx.ranger.repo.TokenExpireException
import org.rfcx.ranger.repo.api.SendLocationApi
import org.rfcx.ranger.util.NotificationHelper
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.RealmHelper
import org.rfcx.ranger.util.isNetWorkAvailable
import org.rfcx.ranger.view.SettingsActivity

/**
 *
 * Service work with location
 * update Ranger location to server.
 */
class LocationTrackerService : Service() {
	
	companion object {
		const val NOTIFICATION_LOCATION_ID = 22
		const val NOTIFICATION_LOCATION_NAME = "Track Ranger location"
		const val NOTIFICATION_LOCATION_CHANNEL_ID = "Location"
		private const val locationUploadRate: Long = 60 * 1000 // a minuit
		
		val locationRequest = LocationRequest().apply {
			priority = LocationRequest.PRIORITY_HIGH_ACCURACY
		}
		
		private const val LOCATION_INTERVAL = 1000L * 20L // 20 seconds
		private const val LOCATION_DISTANCE = 0f// 0 meter
		
		const val TAG = "LocationTrackerService"
	}
	
	private val binder = LocationTrackerServiceBinder()
	
	private var mLocationManager: LocationManager? = null
	
	private val locationListener = object : android.location.LocationListener {
		
		override fun onLocationChanged(p0: Location?) {
			p0?.let {
				getNotificationManager().notify(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(it, true))
				saveLocation(it)
				Log.i(TAG, "${it.longitude} , ${it.longitude}")
				
				if (BuildConfig.DEBUG) playSound()
			}
		}
		
		override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
			
			Log.d(TAG, "onStatusChanged $p0 $p1")
			
			if (p1 == LocationProvider.TEMPORARILY_UNAVAILABLE) {
				getNotificationManager().notify(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(null, true))
			} else if (p1 == LocationProvider.OUT_OF_SERVICE) {
				getNotificationManager().notify(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(null, false))
			}
		}
		
		override fun onProviderEnabled(p0: String?) {
			Log.d(TAG, "onProviderEnabled $p0")
			getNotificationManager().notify(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(null, true))
		}
		
		override fun onProviderDisabled(p0: String?) {
			Log.d(TAG, "onProviderDisabled $p0")
			getNotificationManager().notify(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(null, false))
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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createNotificationChannel()
		}
		val notification = createLocationTrackerNotification(null, true)
		startForeground(NOTIFICATION_LOCATION_ID, notification)
		mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
		try {
			mLocationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListener)
			startForeground(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(null, true))
		} catch (ex: java.lang.SecurityException) {
			ex.printStackTrace()
			Log.w(TAG, "fail to request location update, ignore", ex)
		} catch (ex: IllegalArgumentException) {
			ex.printStackTrace()
			Log.w(TAG, "gps provider does not exist " + ex.message)
		}
		
		try {
			mLocationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListener)
			this.startForeground(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(null, true))
		} catch (ex: java.lang.SecurityException) {
			ex.printStackTrace()
			Log.i(TAG, "fail to request location update, ignore", ex)
		} catch (ex: IllegalArgumentException) {
			ex.printStackTrace()
			Log.d(TAG, "gps provider does not exist " + ex.message)
		}
	}
	
	fun stopTraker() {
		stopForeground(true)
	}
	
	override fun onDestroy() {
		super.onDestroy()
		Log.e(TAG, "onDestroy")
		mLocationManager?.removeUpdates(locationListener)
	}
	
	
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		super.onStartCommand(intent, flags, startId)
		return START_NOT_STICKY
	}
	
	inner class LocationTrackerServiceBinder : Binder() {
		val trackerService: LocationTrackerService
			get() = this@LocationTrackerService
	}
	
	private fun createLocationTrackerNotification(location: Location?, isLocationAvailability: Boolean): Notification {
		val intent = Intent(this, SettingsActivity::class.java)
		val pendingIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT)
		return NotificationCompat.Builder(this, NOTIFICATION_LOCATION_CHANNEL_ID).apply {
			setContentTitle(getString(R.string.notification_location_title))
			if (isLocationAvailability) {
				location?.let {
					setContentText("${it.latitude}, ${it.longitude}")
				} ?: kotlin.run {
					setContentText(getString(R.string.notification_location_loading))
				}
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
		RealmHelper.getInstance().saveLocation(
				RangerLocation(latitude = location.latitude, longitude = location.longitude))
	}


	private fun sentLocation() {
		if (!isNetWorkAvailable()) return
		val lastLocationUpload = Preferences.getInstance(this@LocationTrackerService).getLong(Preferences.LASTED_LOCATION_UPLOAD, 0)
		if (System.currentTimeMillis() - lastLocationUpload < locationUploadRate) {
			return
		}
		// Store last upload location
		Preferences.getInstance(this@LocationTrackerService).putLong(Preferences.LASTED_LOCATION_UPLOAD, System.currentTimeMillis())
		
		val locations = RealmHelper.getInstance().getLocations()
		if (locations.isEmpty()) return
		SendLocationApi().checkIn(this, locations, object : SendLocationApi.SendLocationCallBack {
			override fun onSuccess() {
				// Remove locations are Sent!
				RealmHelper.getInstance().removeSentLocation(locations)
			}
			
			override fun onFailed(t: Throwable?, message: String?) {
				if (t is TokenExpireException) {
					NotificationHelper.getInstance().showLoginNotification(this@LocationTrackerService)
					stopForeground(true)
					stopSelf()
				}
			}
		})
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
	
}