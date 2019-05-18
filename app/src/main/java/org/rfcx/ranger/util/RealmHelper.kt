package org.rfcx.ranger.util

import io.realm.Realm
import io.realm.RealmConfiguration
import org.rfcx.ranger.RangerRealmlMigration
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.message.Message

/**
 * CRUD interface for Realm
 */

class RealmHelper {
	
	companion object {
		@Volatile
		private var INSTANCE: RealmHelper? = null
		
		private const val schemaVersion = 3L
		
		fun getInstance(): RealmHelper =
				INSTANCE ?: synchronized(this) {
					INSTANCE ?: RealmHelper().also { INSTANCE = it }
				}
		
		fun migrationConfig(): RealmConfiguration {
			return RealmConfiguration.Builder().apply {
				schemaVersion(schemaVersion)
				migration(RangerRealmlMigration())
			}.build()
		}
		
		fun defaultConfig(): RealmConfiguration {
			return RealmConfiguration.Builder().apply {
				schemaVersion(schemaVersion)
				deleteRealmIfMigrationNeeded()
			}.build()
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
	
}