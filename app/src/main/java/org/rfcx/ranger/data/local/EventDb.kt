package org.rfcx.ranger.data.local

import io.realm.Realm
import org.rfcx.ranger.entity.event.EventRealmObject

class EventDb {
	
	fun save(eventObj: EventRealmObject) {
		Realm.getDefaultInstance().use { it ->
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
		return Realm.getDefaultInstance().where(EventRealmObject::class.java).equalTo("eventGuId", eventGuid).findFirst()
				?.review
	}
}