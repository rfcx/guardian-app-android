package org.rfcx.ranger

import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration

class RangerRealmlMigration : RealmMigration {
	
	override fun migrate(c: DynamicRealm, oldVersion: Long, newVersion: Long) {
		if (oldVersion < 3L && newVersion >= 3L) {
			migrateToV3(c)
		}
		if (oldVersion < 4L && newVersion >= 4L) {
			migrateToV4(c)
		}
		if (oldVersion < 5L && newVersion >= 5L) {
			migrateToV5(c)
		}
		if (oldVersion < 6L && newVersion >= 6L) {
			migrateToV6(c)
		}
	}
	
	private fun migrateToV3(realm: DynamicRealm) {
		
		// Add field guid to Report
		val report = realm.schema.get("Report")
		report?.apply {
			addField("guid", String::class.java)
		}
		
		// Add ImageReport class
		val reportImage = realm.schema.create("ReportImage")
		reportImage.apply {
			addField("id", Int::class.java, FieldAttribute.PRIMARY_KEY)
			addField("reportId", Int::class.java)
			addField("guid", String::class.java)
			addField("imageUrl", String::class.java)
			addField("createAt", String::class.java)
			setRequired("createAt", true)
			addField("syncState", Int::class.java)
		}
	}
	
	private fun migrateToV4(realm: DynamicRealm) {
		val reportImage = realm.schema.get("ReportImage")
		reportImage?.apply {
			addField("remotePath", String::class.java)
			renameField("imageUrl", "localPath")
			setRequired("localPath", true)
		}
	}
	
	private fun migrateToV5(realm: DynamicRealm) {
		val event = realm.schema.get("Event")
		event?.apply {
			addField("aiGuid", String::class.java)
		}
	}
	
	private fun migrateToV6(realm: DynamicRealm) {
		val report = realm.schema.get("Report")
		report?.apply {
			renameField("ageEstimate", "ageEstimateRaw")
			removeField("distanceEstimate")
		}
	}
}