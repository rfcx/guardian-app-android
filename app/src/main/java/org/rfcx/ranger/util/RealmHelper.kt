package org.rfcx.ranger.util

import io.realm.RealmConfiguration
import org.rfcx.ranger.RangerRealmMigration

/**
 * CRUD interface for Realm
 */

class RealmHelper {
	
	companion object {
		private const val schemaVersion = 11L
		
		fun migrationConfig(): RealmConfiguration {
			return RealmConfiguration.Builder().apply {
				schemaVersion(schemaVersion)
				migration(RangerRealmMigration())
			}.build()
		}
		
		fun fallbackConfig(): RealmConfiguration {
			return RealmConfiguration.Builder().apply {
				schemaVersion(schemaVersion)
				deleteRealmIfMigrationNeeded()
			}.build()
		}
	}
}