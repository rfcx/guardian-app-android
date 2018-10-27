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
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.android.gms.location.*
import org.rfcx.ranger.R
import org.rfcx.ranger.repo.TokenExpireException
import org.rfcx.ranger.repo.api.SendLocationApi
import org.rfcx.ranger.util.DateHelper
import org.rfcx.ranger.util.NotificationHelper
import org.rfcx.ranger.view.SettingActivity

/**
 * Created by Jingjoeh on 10/7/2017 AD.
 *
 * Service work with location
 * update Ranger location to server.
 */
class LocationTrackerService : Service() {
	
	companion object {
		const val NOTIFICATION_LOCATION_ID = 22
		const val NOTIFICATION_LOCATION_NAME = "Track Ranger location"
		const val NOTIFICATION_LOCATION_CHANNEL_ID = "Location"
	}
	
	private val tag = LocationTrackerService::class.java.simpleName
	private val binder = SendLocationLocationServiceBinder()
	private val intervalLocationUpdate: Long = 30 * 1000 // 30 seconds
	private var fusedLocationClient: FusedLocationProviderClient? = null
	
	private var locationCallback: LocationCallback = object : LocationCallback() {
		override fun onLocationResult(locationResult: LocationResult?) {
			super.onLocationResult(locationResult)
			Log.d(tag, "${locationResult?.lastLocation?.latitude} ${locationResult?.lastLocation?.longitude}")
			
			getNotificationManager().notify(NOTIFICATION_LOCATION_ID,
					createLocationTrackerNotification(locationResult?.lastLocation))
			
			locationResult?.lastLocation?.let {
				sentLocation(it)
			}
		}
		
	}
	
	override fun onBind(p0: Intent?): IBinder? {
		return binder
	}
	
	override fun onCreate() {
		super.onCreate()
		
		startTracker()
	}
	
	private fun startTracker() {
		val check = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
		
		if (!check) {
			this.stopSelf()
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createNotificationChannel()
		}
		
		val locationRequest = LocationRequest()
		locationRequest.interval = intervalLocationUpdate
		locationRequest.priority = LocationRequest.PRIORITY_LOW_POWER
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
		var lastLocation: Location? = null
		fusedLocationClient?.lastLocation?.addOnSuccessListener {
			Log.i(tag, "${it?.latitude} ${it?.longitude}")
			lastLocation = it
			if (it != null) {
				sentLocation(it)
			}
		}
		fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null)
		this.startForeground(NOTIFICATION_LOCATION_ID, createLocationTrackerNotification(lastLocation))
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
	
	private fun createLocationTrackerNotification(location: Location?): Notification {
		val intent = Intent(this, SettingActivity::class.java)
		val pendingIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT)
		return NotificationCompat.Builder(this, NOTIFICATION_LOCATION_CHANNEL_ID).apply {
			setContentTitle(getString(R.string.notification_location_title))
			location?.let {
				setContentText("${it.latitude}, ${it.longitude}")
			} ?: kotlin.run {
				setContentText(getString(R.string.notification_location_loading))
			}
			
			setSmallIcon(R.drawable.chainsaw_green)
			setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_my_location_green_24dp))
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
	
	private fun sentLocation(location: Location) {
		SendLocationApi().checkIn(this, location.latitude, location.longitude, DateHelper.getIsoTime(), object : SendLocationApi.SendLocationCallBack {
			override fun onSuccess() {
				// Success do nothing
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