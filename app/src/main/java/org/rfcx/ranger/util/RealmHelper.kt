package org.rfcx.ranger.util

import io.realm.Realm
import io.realm.RealmConfiguration
import org.rfcx.ranger.RangerRealmMigration

/**
 * CRUD interface for Realm
 */

class RealmHelper {
	
	companion object {
		private const val schemaVersion = 7L
		
		fun migrationConfig(): RealmConfiguration {
			return RealmConfiguration.Builder().apply {
				schemaVersion(schemaVersion)
				migration(RangerRealmMigration())
			}.build()
		}
		
		fun defaultConfig(): RealmConfiguration {
			return RealmConfiguration.Builder().apply {
				schemaVersion(schemaVersion)
				deleteRealmIfMigrationNeeded()
			}.build()
		}
	}
}