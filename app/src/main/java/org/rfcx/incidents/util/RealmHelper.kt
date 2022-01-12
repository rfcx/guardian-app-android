package org.rfcx.incidents.util

import io.realm.RealmConfiguration
import org.rfcx.incidents.RangerRealmMigration

/**
 * CRUD interface for Realm
 */

class RealmHelper {
	
	companion object {
		private const val schemaVersion = 18L
		
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
