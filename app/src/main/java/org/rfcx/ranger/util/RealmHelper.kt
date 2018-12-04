package org.rfcx.ranger.util

import io.realm.Realm
import io.realm.RealmResults
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.guardian.GuardianGroup
import org.rfcx.ranger.entity.location.RangerLocation
import org.rfcx.ranger.entity.message.Message

/**
 * CRUD interface for Realm
 */

class RealmHelper {
	
	companion object {
		@Volatile
		private var INSTANCE: RealmHelper? = null
		
		fun getInstance(): RealmHelper =
				INSTANCE ?: synchronized(this) {
					INSTANCE ?: RealmHelper().also { INSTANCE = it }
				}
	}
	
	fun updateOpenedMessage(message: Message) {
		message.isOpened = true
		val realm = Realm.getDefaultInstance()
		realm.beginTransaction()
		realm.insertOrUpdate(message)
		realm.commitTransaction()
		realm.close()
	}
	
	fun updateOpenedEvent(event: Event) {
		event.isOpened = true
		val realm = Realm.getDefaultInstance()
		realm.beginTransaction()
		realm.insertOrUpdate(event)
		realm.commitTransaction()
		realm.close()
	}
	
	fun updateConfirmedEvent(event: Event) {
		event.isConfirmed = true
		val realm = Realm.getDefaultInstance()
		realm.beginTransaction()
		realm.insertOrUpdate(event)
		realm.commitTransaction()
		realm.close()
	}
	
	fun findLocalEvent(guid: String): Event? {
		return Realm.getDefaultInstance().use { realm ->
			val event = realm.where(Event::class.java)
					.equalTo(Event.eventGUID, guid).findFirst()
			
			event?.let { realm.copyFromRealm(event) }
		}
	}
	
	fun findLocalMessage(guid: String): Message? {
		return Realm.getDefaultInstance().use { realm ->
			val event = realm.where(Message::class.java)
					.equalTo(Message.messageGUID, guid).findFirst()
			
			event?.let { realm.copyFromRealm(event) }
		}
	}
	
	fun guardianGroups(): RealmResults<GuardianGroup> {
		val realm = Realm.getDefaultInstance()
		return realm.where(GuardianGroup::class.java).findAll()
	}
	
	fun saveGuardianGroups(groups: List<GuardianGroup>) {
		val realm = Realm.getDefaultInstance()
		realm.beginTransaction()
		realm.insertOrUpdate(groups)
		realm.commitTransaction()
		realm.close()
	}
	
	fun saveLocation(rangerLocation: RangerLocation) {
		Realm.getDefaultInstance().use { realm ->
			realm.executeTransaction {
				it.insertOrUpdate(rangerLocation)
			}
		}
	}
	
	fun getLocations(): List<RangerLocation> {
		val locations: ArrayList<RangerLocation> = ArrayList()
		Realm.getDefaultInstance().use { realm ->
			realm.executeTransaction {
				val result = it.where(RangerLocation::class.java)
						.findAll()
				locations.addAll(it.copyFromRealm(result))
			}
		}
		return locations
	}
	
	fun removeSentLocation(input: List<RangerLocation>) {
		Realm.getDefaultInstance().use { realm ->
			realm.executeTransaction {
				for (location in input) {
					val shouldDelete = it.where(RangerLocation::class.java)
							.equalTo("time", location.time).findAll()
					shouldDelete?.deleteAllFromRealm()
				}
			}
		}
	}
}