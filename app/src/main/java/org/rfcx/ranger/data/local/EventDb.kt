package org.rfcx.ranger.data.local

import io.realm.Realm
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventReview

class EventDb {
	
	fun getEvents(): List<Event> {
		return Realm.getDefaultInstance().copyFromRealm(
				Realm.getDefaultInstance().where(Event::class.java).findAllAsync())
	}
	
	fun getEventsSync(): List<Event> {
		val list = arrayListOf<Event>()
		Realm.getDefaultInstance().use { it ->
			it.executeTransaction {
				val events = it.where(Event::class.java)
						.findAll()
				list.addAll(it.copyFromRealm(events))
			}
		}
		return  list
	}
	
	fun save(eventObj: EventReview) {
		Realm.getDefaultInstance().use { it ->
			it.executeTransaction {
				it.copyToRealmOrUpdate(eventObj)
			}
		}
	}
	
	fun saveEvents(events: List<Event>) {
		Realm.getDefaultInstance().use { it ->
			it.executeTransaction {
				it.delete(Event::class.java)
				it.insertOrUpdate(events)
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