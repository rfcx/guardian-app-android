package org.rfcx.ranger.util

import io.realm.RealmConfiguration
import org.rfcx.ranger.RangerRealmlMigration

/**
 * CRUD interface for Realm
 */

class RealmHelper {
	
	companion object {
		@Volatile
		private var INSTANCE: RealmHelper? = null
		
		private const val schemaVersion = 6L
		
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
}