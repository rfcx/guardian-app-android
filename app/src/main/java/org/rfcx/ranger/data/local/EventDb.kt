package org.rfcx.ranger.data.local

import io.realm.Realm
import io.realm.RealmResults
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventReview

class EventDb(val realm: Realm) {
	
	fun getEvents(): List<Event> {
		return realm.copyFromRealm(realm.where(Event::class.java).findAllAsync())
	}
	
	fun getCount(): Long {
		return realm.where(Event::class.java).count()
	}
	
	fun getAllResultsAsync(): RealmResults<Event> {
		return realm.where(Event::class.java).findAllAsync()
	}
	
	fun getEventsSync(): List<Event> {
		val list = arrayListOf<Event>()
		realm.use { it ->
			it.executeTransaction {
				val events = it.where(Event::class.java)
						.findAll()
				list.addAll(it.copyFromRealm(events))
			}
		}
		return list
	}
	
	fun save(eventObj: EventReview) {
		realm.use { it ->
			it.executeTransaction {
				it.copyToRealmOrUpdate(eventObj)
			}
		}
	}
	
	fun saveEvents(events: List<Event>) {
		realm.use { it ->
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
		realm.use {
				reviewVal = it.where(EventReview::class.java)
						.equalTo("eventGuId", eventGuid).findFirst()
						?.review
		}
		return reviewVal
	}
	
	fun lockReviewEventUnSent(): List<EventReview> {
		val unsentList = arrayListOf<EventReview>()
		realm.use { it ->
			it.executeTransaction { realm ->
				val unsent = realm.where(EventReview::class.java)
						.equalTo("syncState", EventReview.UNSENT).findAll()
				unsentList.addAll(it.copyFromRealm(unsent))
				unsent.forEach {
					it.syncState = EventReview.SENDING
				}
			}
		}
		
		return unsentList
	}
	
	fun markReviewEventSyncState(eventGuid: String, syncState: Int) {
		realm.use { it ->
			it.executeTransaction {
				val event = it.where(EventReview::class.java)
						.equalTo("eventGuId", eventGuid).findFirst()
				event?.syncState = syncState
			}
		}
	}
	
	fun deleteAllEvents() {
		realm.use { it ->
			it.executeTransaction {
				it.delete(Event::class.java)
			}
		}
	}
}