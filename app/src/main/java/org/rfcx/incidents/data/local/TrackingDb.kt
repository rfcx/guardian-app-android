package org.rfcx.incidents.data.local

import android.content.Context
import io.realm.Realm
import io.realm.RealmResults
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.entity.location.Coordinate
import org.rfcx.incidents.entity.location.Tracking
import java.util.*

class TrackingDb(private val realm: Realm) {
    fun insertOrUpdate(tracking: Tracking, coordinate: Coordinate) {
        realm.executeTransaction {
            val track = it.where(Tracking::class.java).equalTo(Tracking.TRACKING_ID, tracking.id).findFirst()
            if (track != null) {
                tracking.startAt = track.startAt
                tracking.points = track.points
            }

            tracking.stopAt = Date()
            tracking.points.add(coordinate)
            it.insertOrUpdate(tracking)
        }
    }

    fun deleteTracking(id: Int, context: Context) {
        realm.executeTransaction {
            val tracking = it.where(Tracking::class.java).equalTo(Tracking.TRACKING_ID, id).findFirst()
            tracking?.deleteFromRealm()

            val preferences = Preferences.getInstance(context)
            preferences.putLong(Preferences.LATEST_GET_LOCATION_TIME, 0L)
        }
    }

    fun getFirstTracking(): Tracking? {
        return realm.where(Tracking::class.java).findFirst()
    }

    fun getAllResultsAsync(): RealmResults<Tracking> {
        return realm.where(Tracking::class.java).findAllAsync()
    }
}
