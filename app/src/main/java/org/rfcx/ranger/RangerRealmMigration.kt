package org.rfcx.ranger

import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration
import org.rfcx.ranger.entity.event.EventReview
import org.rfcx.ranger.util.legacyDateParser
import java.util.*

@Suppress("DEPRECATION")
class RangerRealmMigration : RealmMigration {
	
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
		if (oldVersion < 7L && newVersion >= 7L) {
			migrateToV7(c)
		}
		if (oldVersion < 8L && newVersion >= 8L) {
			migrateToV8(c)
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
		
		// Add EventReview class
		val eventReview = realm.schema.create("EventReview")
		eventReview.apply {
			addField("eventGuId", String::class.java, FieldAttribute.PRIMARY_KEY)
					.setRequired("eventGuId", true)
			addField("review", String::class.java)
		}
	}
	
	private fun migrateToV7(realm: DynamicRealm) {
		// Edit Report's reportedAt to Date
		val report = realm.schema.get("Report")
		report?.apply {
			addField("reportedAt_tmp", Date::class.java)
					.setNullable("reportedAt_tmp", false)
			
			transform { obj ->
				val reportedAt = obj.getString("reportedAt")
				val date = legacyDateParser(reportedAt)
				obj.setDate("reportedAt_tmp", date)
			}
			
			removeField("reportedAt")
			renameField("reportedAt_tmp", "reportedAt")
		}
		
		// Edit ReportImage's createAt to Date
		val reportImage = realm.schema.get("ReportImage")
		reportImage?.apply {
			addField("createAt_tmp", Date::class.java)
					.setNullable("createAt_tmp", false)
			transform { obj ->
				val createAt = obj.getString("createAt")
				val date = legacyDateParser(createAt)
				obj.setDate("createAt_tmp", date)
			}
			
			removeField("createAt")
			renameField("createAt_tmp", "createAt")
		}
		
		// Edit CheckIn's time to Date
		val checkIn = realm.schema.get("CheckIn")
		checkIn?.apply {
			addField("time_tmp", Date::class.java)
					.setNullable("time_tmp", false)
			transform { obj ->
				val time = obj.getString("time")
				val date = legacyDateParser(time)
				obj.setDate("time_tmp", date)
			}
			
			removeField("time")
			renameField("time_tmp", "time")
		}
		
		// Edit Event's beginsAt and endAt to Date
		val event = realm.schema.get("Event")
		event?.apply {
			addField("beginsAt_tmp", Date::class.java)
					.setNullable("beginsAt_tmp", false)
			addField("endAt_tmp", Date::class.java)
					.setNullable("endAt_tmp", false)
			
			transform {
				// beginsAt
				val beginsAt = it.getString("beginsAt")
				val date = legacyDateParser(beginsAt)
				it.setDate("beginsAt_tmp", date)
				
				// endAt
				val endAt = it.getString("endAt")
				val d = legacyDateParser(endAt)
				it.setDate("endAt_tmp", d)
			}
			
			removeField("beginsAt")
			renameField("beginsAt_tmp", "beginsAt")
			
			removeField("endAt")
			renameField("endAt_tmp", "endAt")
			removeField("isOpened")
			
		}
		
		val eventReview = realm.schema.get("EventReview")
		eventReview?.apply {
			addField("syncState", Int::class.java)
			transform { obj ->
				obj.setInt("syncState", EventReview.SENT)
			}
		}
	}
	
	private fun migrateToV8(realm: DynamicRealm) {
		val windows = realm.schema.create("EventWindow")
		windows?.apply {
			addField("guid", String::class.java, FieldAttribute.PRIMARY_KEY)
					.setRequired("guid", true)
			addField("confidence", Double::class.java)
			addField("start", Int::class.java)
			addField("end", Int::class.java)
		}
		
		val audio = realm.schema.get("Audio")
		audio?.apply {
			removeField("mp3")
		}

		val event = realm.schema.get("Event")
		event?.apply {
			//review
			addField("reviewConfirmed", Boolean::class.java).setNullable("reviewConfirmed", true)
			addField("reviewCreated", Date::class.java).setRequired("reviewCreated", true)
			
			//windows
			addRealmListField("windows", windows)
			
			//audio
			addField("audioOpusUrl", String::class.java).setRequired("audioOpusUrl", true)
			addField("audioPngUrl", String::class.java).setRequired("audioPngUrl", true)
			
			//event
			addField("audioDuration", Long::class.java)
			addField("rejectedCount", Int::class.java)
			addField("confirmedCount", Int::class.java)
			addField("label", String::class.java).setRequired("label", true)
			addField("audioId", String::class.java).setRequired("audioId", true)
			
			renameField("event_guid", "id")
			renameField("guardianShortname", "guardianName")
			renameField("guardianGUID", "guardianId")
			
			setRequired("value", true)
			setRequired("guardianId", true)
			setRequired("guardianName", true)
			setRequired("site", true)
			
			removeField("endAt")
			removeField("aiGuid")
			removeField("audio")
			removeField("reviewerConfirmed")
			removeField("confidence")
			removeField("timezone")
			removeField("audioGUID")
		}
	}
	
	override fun hashCode(): Int {
		return 1
	}
	
	override fun equals(other: Any?): Boolean {
		return other.hashCode() == hashCode()
	}
}