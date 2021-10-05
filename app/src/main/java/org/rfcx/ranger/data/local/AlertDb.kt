package org.rfcx.ranger.data.local

import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.deleteFromRealm
import org.rfcx.ranger.data.api.events.ResponseEvent
import org.rfcx.ranger.data.api.events.toAlert
import org.rfcx.ranger.entity.alert.Alert

class AlertDb(private val realm: Realm) {
	fun insertAlert(response: ResponseEvent) {
		realm.executeTransaction {
			val alert =
					it.where(Alert::class.java)
							.equalTo(Alert.ALERT_SERVER_ID, response.id)
							.findFirst()
			
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
	
	fun getAllResultsAsync(): RealmResults<Alert> {
		return realm.where(Alert::class.java).findAllAsync()
	}
	
	fun deleteAlert(id: String) {
		realm.executeTransaction {
			val alert =
					it.where(Alert::class.java).equalTo(Alert.ALERT_STREAM_ID, id)
							.findFirst()
			alert?.deleteFromRealm()
		}
	}
}
