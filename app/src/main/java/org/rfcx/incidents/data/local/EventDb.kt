package org.rfcx.incidents.data.local

import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import org.rfcx.incidents.entity.CachedEndpoint
import org.rfcx.incidents.entity.event.Event
import org.rfcx.incidents.entity.event.EventReview

class EventDb(val realm: Realm) {
	
	
	fun getEvents(): List<Event> {
		return realm.copyFromRealm(realm.where(Event::class.java).sort("beginsAt", Sort.DESCENDING).findAll())
	}
	
	fun getEvent(eventGuid: String): Event? {
		val event = realm.where(Event::class.java)
				.equalTo("id", eventGuid)
				.findFirst()
		return if (event != null) realm.copyFromRealm(event) else null
	}
	
	fun getCount(): Long {
		return realm.where(Event::class.java).count()
	}
	
	fun getAllResultsAsync(): RealmResults<Event> {
		return realm.where(Event::class.java).findAllAsync()
	}
	
	fun getByGuardianName(guardianName: String): RealmResults<Event> {
		return realm.where(Event::class.java).equalTo("guardianName", guardianName).findAllAsync()
	}
	
	fun getEventsSync(): List<Event> {
		val list = arrayListOf<Event>()
		realm.use { it ->
			it.executeTransaction {
				val events = it.where(Event::class.java)
						.sort("beginsAt",Sort.DESCENDING)
						.findAll()
				list.addAll(it.copyFromRealm(events))
			}
		}
		return list
	}
	
	fun save(eventObj: EventReview) {
		Realm.getDefaultInstance().use { realm ->
			realm.executeTransaction {
				it.copyToRealmOrUpdate(eventObj)
			}
		}
	}
	
	fun saveEvents(events: List<Event>) {
		Realm.getDefaultInstance().use { it ->
			it.executeTransaction {
				it.insertOrUpdate(events)
			}
		}
	}
	
	fun saveEvent(event: Event) {
		Realm.getDefaultInstance().use { it ->
			it.executeTransaction {
				it.insertOrUpdate(event)
			}
		}
	}
	
	/**
	 * @param eventGuid :String of Event
	 * return event state of review -> null,confirm,reject
	 */
	fun getEventState(eventGuid: String): String? {
		var reviewVal: String? = null
		Realm.getDefaultInstance().use {
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
	
	fun lockReviewEventUnread(events: List<Event>): List<Event> {
		val readList = arrayListOf<EventReview>()
		realm.use {
			it.executeTransaction { realm ->
				val unread = realm.where(EventReview::class.java).findAll()
				readList.addAll(it.copyFromRealm(unread))
			}
		}
		
		val guidEventRead = readList.mapTo(arrayListOf(), {it.eventGuId})
		return events.filter { !guidEventRead.contains(it.id) } // unread
	}
	
	fun markReviewEventSyncState(eventGuid: String, syncState: Int) {
		realm.use {
			it.executeTransaction {realm ->
				val event = realm.where(EventReview::class.java)
						.equalTo("eventGuId", eventGuid).findFirst()
				event?.syncState = syncState
			}
		}
	}
	
	fun deleteAllEvents(callback: (Boolean) -> Unit) {
		realm.use { realm ->
			realm.executeTransactionAsync({ bgRealm ->
				bgRealm.delete(Event::class.java)
				// clear cache endpoint
				bgRealm.where(CachedEndpoint::class.java).like(CachedEndpoint.FIELD_ENDPOINT,
						"guardians/group/*").findAll().deleteAllFromRealm()
				bgRealm.where(CachedEndpoint::class.java).like(CachedEndpoint.FIELD_ENDPOINT,
						"v2/events/?guardian_groups[]=*").findAll().deleteAllFromRealm()
			}, {
				// success
				callback(true)
			}, {
				// fail
				callback(false)
			})
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
