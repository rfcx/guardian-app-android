package org.rfcx.ranger.data.local

import io.realm.Realm
import org.rfcx.ranger.entity.event.EventRealmObject

class EventDb(private val realm: Realm = Realm.getDefaultInstance()) {
	
	fun save(eventObj: EventRealmObject) {
		realm.use { it ->
			it.executeTransaction {
				it.insertOrUpdate(eventObj)
			}
		}
	}
	
	/**
	 * @param eventGuid :String of Event
	 * return event state of review -> null,confirm,reject
	 */
	fun getEventState(eventGuid: String): String? {
		return realm.where(EventRealmObject::class.java).equalTo("eventGuId", eventGuid).findFirst()
				?.review
	}
}