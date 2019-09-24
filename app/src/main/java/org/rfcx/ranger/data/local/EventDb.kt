package org.rfcx.ranger.data.local

import io.realm.Realm
import org.rfcx.ranger.entity.event.EventReview

class EventDb {
	
	fun save(eventObj: EventReview) {
		Realm.getDefaultInstance().use { it ->
			it.executeTransaction {
				it.copyToRealmOrUpdate(eventObj)
			}
		}
		
		
	}
	
	/**
	 * @param eventGuid :String of Event
	 * return event state of review -> null,confirm,reject
	 */
	fun getEventState(eventGuid: String): String? {
		var reviewVal: String? = null
		Realm.getDefaultInstance().use { it ->
			it.executeTransaction {
				reviewVal = it.where(EventReview::class.java)
						.equalTo("eventGuId", eventGuid).findFirst()
						?.review
			}
		}
		return reviewVal
	}
}