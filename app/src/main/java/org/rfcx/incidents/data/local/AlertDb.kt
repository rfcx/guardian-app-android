package org.rfcx.incidents.data.local

import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.deleteFromRealm
import org.rfcx.incidents.data.api.events.ResponseEvent
import org.rfcx.incidents.data.api.events.toAlert
import org.rfcx.incidents.entity.alert.Alert
import java.util.*

class AlertDb(private val realm: Realm) {
	fun insertAlert(response: ResponseEvent) {
		realm.executeTransaction {
			val alert = it.where(Alert::class.java).equalTo(Alert.ALERT_SERVER_ID, response.id).findFirst()
			
			if (alert == null) {
				val alertObj = response.toAlert()
				val id = (it.where(Alert::class.java).max(Alert.ALERT_ID)
						?.toInt() ?: 0) + 1
				alertObj.id = id
				it.insert(alertObj)
			}
		}
	}
	
	fun getAlertCount(streamId: String): Long = realm.where(Alert::class.java).equalTo(Alert.ALERT_STREAM_ID, streamId).count()
	
	fun getAlerts(streamId: String): List<Alert> = realm.where(Alert::class.java).equalTo(Alert.ALERT_STREAM_ID, streamId).sort(Alert.ALERT_START, Sort.ASCENDING).findAll()
	
	fun getStartTimeOfAlerts(streamId: String): Date? {
		val alerts = realm.where(Alert::class.java).equalTo(Alert.ALERT_STREAM_ID, streamId).sort(Alert.ALERT_START, Sort.ASCENDING).findAll()
		return if (alerts.isNotEmpty()) alerts[0]?.start else null
	}
	
	fun getAllResultsAsync(): RealmResults<Alert> {
		return realm.where(Alert::class.java).sort(Alert.ALERT_START, Sort.DESCENDING).findAllAsync()
	}
	
	fun deleteAlertsByStreamId(id: String) {
		realm.executeTransaction {
			val alert = it.where(Alert::class.java).equalTo(Alert.ALERT_STREAM_ID, id).findAll()
			alert?.forEach { a ->
				a.deleteFromRealm()
			}
		}
	}
}
