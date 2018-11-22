package org.rfcx.ranger.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import org.rfcx.ranger.R
import org.rfcx.ranger.entity.location.RangerLocation
import org.rfcx.ranger.repo.TokenExpireException
import org.rfcx.ranger.repo.api.SendLocationApi
import org.rfcx.ranger.util.*
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
		private const val intervalLocationUpdate: Long = 30 * 1000 // 30 seconds
		private const val fastestIntervalLocationUpdate: Long = 20 * 1000
		private const val locationUploadRate: Long = 60 * 1000 // a minuit
		
		val locationRequest = LocationRequest().apply {
			interval = intervalLocationUpdate
			fastestInterval = fastestIntervalLocationUpdate
			priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
		}
	}
	
	private val tag = LocationTrackerService::class.java.simpleName
	private val binder = SendLocationLocationServiceBinder()
	
	private var fusedLocationClient: FusedLocationProviderClient? = null
	
	private var locationCallback: LocationCallback = object : LocationCallback() {
		override fun onLocationResult(locationResult: LocationResult?) {
			super.onLocationResult(locationResult)
			
			getNotificationManager().notify(NOTIFICATION_LOCATION_ID,
					createLocationTrackerNotification(locationResult?.lastLocation, true))
			
			locationResult?.lastLocation?.let {
				saveLocation(it)
			}
			
			sentLocation()
			
		}
		
		override fun onLocationAvailability(p0: LocationAvailability?) {
			super.onLocationAvailability(p0)
			if (p0?.isLocationAvailable == false) {
				// user turn off location on setting or something
				getNotificationManager().notify(NOTIFICATION_LOCATION_ID,
						createLocationTrackerNotification(null, false))
			}
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
		
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
		var lastLocation: Location? = null
		fusedLocationClient?.lastLocation?.addOnSuccessListener {
			Log.i(tag, "${it?.latitude} ${it?.longitude}")
			lastLocation = it
			if (it != null) {
				saveLocation(it)
			}
		}
		fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null)
		this.startForeground(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(lastLocation, true))
		
		sentLocation()
	}
	
	override fun onDestroy() {
		super.onDestroy()
		locationCallback.let { fusedLocationClient?.removeLocationUpdates(it) }
	}
	
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		return START_NOT_STICKY
	}
	
	inner class SendLocationLocationServiceBinder : Binder() {
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
		if(!isNetWorkAvailable()) return
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
	
}