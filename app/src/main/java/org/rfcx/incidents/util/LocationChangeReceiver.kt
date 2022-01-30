package org.rfcx.incidents.util

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import com.google.android.gms.location.LocationResult
import io.realm.Realm
import org.rfcx.incidents.entity.location.Coordinate
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.localdb.TrackingDb

class LocationChangeReceiver : BroadcastReceiver() {
    private val realm by lazy { Realm.getInstance(RealmHelper.migrationConfig()) }
    private val trackingDb by lazy { TrackingDb(realm) }
    private var tracking = Tracking()
    
    companion object {
        private const val FIVE_MINUTES = 5L * 60L * 1000L
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (LocationResult.hasResult(intent)) {
            val locationResult = LocationResult.extractResult(intent)
            val location = locationResult.lastLocation
            if (context != null) {
                if (canSaveLocation(context)) saveLocation(context, location)
            }
        }
    }
    
    private fun canSaveLocation(context: Context): Boolean {
        val lastGetLocationTime = Preferences.getInstance(context).getLong(Preferences.LATEST_GET_LOCATION_TIME, 0L)
        return isInForeground() && System.currentTimeMillis() - lastGetLocationTime >= FIVE_MINUTES
    }
    
    private fun isInForeground(): Boolean {
        val myProcess = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(myProcess)
        return myProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }
    
    private fun saveLocation(context: Context, location: Location) {
        tracking.id = 1
        val coordinate = Coordinate(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = location.altitude
        )
        trackingDb.insertOrUpdate(tracking, coordinate)
        Preferences.getInstance(context).putLong(Preferences.LATEST_GET_LOCATION_TIME, System.currentTimeMillis())
    }
}
