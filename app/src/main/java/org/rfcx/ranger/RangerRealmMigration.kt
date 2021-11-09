package org.rfcx.ranger

import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration
import org.rfcx.ranger.entity.CachedEndpoint
import org.rfcx.ranger.entity.Classification
import org.rfcx.ranger.entity.Incident
import org.rfcx.ranger.entity.Stream
import org.rfcx.ranger.entity.alert.Alert
import org.rfcx.ranger.entity.event.EventReview
import org.rfcx.ranger.entity.location.Coordinate
import org.rfcx.ranger.entity.location.Tracking
import org.rfcx.ranger.entity.location.TrackingFile
import org.rfcx.ranger.entity.project.Project
import org.rfcx.ranger.entity.report.ReportImage
import org.rfcx.ranger.entity.response.Response
import org.rfcx.ranger.entity.response.Voice
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
		if (oldVersion < 9L && newVersion >= 9L) {
			migrateToV9(c)
		}
		if (oldVersion < 10L && newVersion >= 10L) {
			migrateToV10(c)
		}
		if (oldVersion < 11L && newVersion >= 11L) {
			migrateToV11(c)
		}
		if (oldVersion < 12L && newVersion >= 12L) {
			migrateToV12(c)
		}
		if (oldVersion < 13L && newVersion >= 13L) {
			migrateToV13(c)
		}
		if (oldVersion < 14L && newVersion >= 14L) {
			migrateToV14(c)
		}
		if (oldVersion < 15L && newVersion >= 15L) {
			migrateToV15(c)
		}
		if (oldVersion < 16L && newVersion >= 16L) {
			migrateToV16(c)
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
	
	private fun migrateToV9(realm: DynamicRealm) {
		// Add field notes to Report
		val report = realm.schema.get("Report")
		report?.apply {
			addField("notes", String::class.java)
		}
	}
	
	private fun migrateToV10(realm: DynamicRealm) {
		val cachedEndpoint = realm.schema.create(CachedEndpoint.TABEL_NAME)
		cachedEndpoint?.apply {
			addField(CachedEndpoint.FIELD_ENDPOINT, String::class.java, FieldAttribute.PRIMARY_KEY)
			setRequired(CachedEndpoint.FIELD_ENDPOINT, true)
			
			addField(CachedEndpoint.FIELD_UPDATED_AT, Date::class.java)
			setRequired(CachedEndpoint.FIELD_UPDATED_AT, true)
		}
	}
	
	private fun migrateToV11(realm: DynamicRealm) {
		val guardianGroup = realm.schema.get("GuardianGroup")
		guardianGroup?.apply {
			addRealmListField("values", String::class.java)
			
		}
	}
	
	private fun migrateToV12(realm: DynamicRealm) {
		val event = realm.schema.get("Event")
		event?.apply {
			//reviewer
			addField("firstNameReviewer", String::class.java).setRequired("firstNameReviewer", true)
		}
	}
	
	private fun migrateToV13(realm: DynamicRealm) {
		val project = realm.schema.create(Project.TABLE_NAME)
		project.apply {
			addField(Project.PROJECT_ID, Int::class.java, FieldAttribute.PRIMARY_KEY)
			addField(Project.PROJECT_NAME, String::class.java)
					.setRequired(Project.PROJECT_NAME, true)
			addField(Project.PROJECT_SERVER_ID, String::class.java)
			addField(Project.PROJECT_PERMISSIONS, String::class.java)
					.setRequired(Project.PROJECT_PERMISSIONS, true)
		}
	}
	
	private fun migrateToV14(realm: DynamicRealm) {
		val response = realm.schema.create(Response.TABLE_NAME)
		response.apply {
			addField(Response.RESPONSE_ID, Int::class.java, FieldAttribute.PRIMARY_KEY)
			addField(Response.RESPONSE_GUID, String::class.java)
			addField(Response.RESPONSE_INVESTIGATED_AT, Date::class.java)
					.setRequired(Response.RESPONSE_INVESTIGATED_AT, true)
			addField(Response.RESPONSE_STARTED_AT, Date::class.java)
					.setRequired(Response.RESPONSE_STARTED_AT, true)
			addRealmListField(Response.RESPONSE_ANSWERS, Int::class.java)
					.setRequired(Response.RESPONSE_ANSWERS, false)
			addField(Response.RESPONSE_SUBMITTED_AT, Date::class.java)
			addRealmListField(Response.RESPONSE_EVIDENCES, Int::class.java)
					.setRequired(Response.RESPONSE_EVIDENCES, false)
			addField(Response.RESPONSE_LOGGING_SCALE, Int::class.java)
			addField(Response.RESPONSE_DAMAGE_SCALE, Int::class.java)
			addField(Response.RESPONSE_SYNC_STATE, Int::class.java)
			addRealmListField(Response.RESPONSE_RESPONSE_ACTIONS, Int::class.java)
					.setRequired(Response.RESPONSE_RESPONSE_ACTIONS, false)
			addField(Response.RESPONSE_AUDIO_LOCATION, String::class.java)
					.setRequired(Response.RESPONSE_AUDIO_LOCATION, false)
			addField(Response.RESPONSE_NOTE, String::class.java)
			addField(Response.RESPONSE_INCIDENT_REF, String::class.java)
			addField(Response.RESPONSE_STREAM_ID, String::class.java)
					.setRequired(Response.RESPONSE_STREAM_ID, true)
			addField(Response.RESPONSE_STREAM_NAME, String::class.java)
					.setRequired(Response.RESPONSE_STREAM_NAME, true)
		}
		
		val classification = realm.schema.create(Classification.TABLE_NAME)
		classification.apply {
			addField(Classification.CLASSIFICATION_VALUE, String::class.java)
			addField(Classification.CLASSIFICATION_TITLE, String::class.java)
		}
		
		val incident = realm.schema.create(Incident.TABLE_NAME)
		incident.apply {
			addField(Incident.INCIDENT_ID, String::class.java)
			addField(Incident.INCIDENT_CLOSED_AT, Date::class.java)
					.setRequired(Incident.INCIDENT_CLOSED_AT, false)
			addField(Incident.INCIDENT_CREATED_AT, Date::class.java)
		}
		
		val alert = realm.schema.create(Alert.TABLE_NAME)
		alert.apply {
			addField(Alert.ALERT_ID, Int::class.java, FieldAttribute.PRIMARY_KEY)
			addField(Alert.ALERT_SERVER_ID, String::class.java)
			addField(Alert.ALERT_NAME, String::class.java)
			addField(Alert.ALERT_STREAM_ID, String::class.java)
			addField(Alert.ALERT_PROJECT_ID, String::class.java)
			addField(Alert.ALERT_CREATED_AT, Date::class.java)
			addField(Alert.ALERT_START, Date::class.java)
			addField(Alert.ALERT_END, Date::class.java)
			addRealmObjectField(Alert.ALERT_CLASSIFICATION, classification)
			addRealmObjectField(Alert.ALERT_INCIDENT, incident)
		}
		
		val stream = realm.schema.create(Stream.TABLE_NAME)
		stream.apply {
			addField(Stream.STREAM_ID, Int::class.java, FieldAttribute.PRIMARY_KEY)
			addField(Stream.STREAM_SERVER_ID, String::class.java)
			addField(Stream.STREAM_NAME, String::class.java)
			addField(Stream.STREAM_LATITUDE, Double::class.java)
			addField(Stream.STREAM_LONGITUDE, Double::class.java)
			addField(Stream.STREAM_PROJECT_SERVER_ID, String::class.java)
		}
	}
	
	private fun migrateToV15(realm: DynamicRealm) {
		val reportImage = realm.schema.get(ReportImage.TABLE_NAME)
		reportImage?.apply {
			addField(ReportImage.FIELD_REPORT_SERVER_ID, String::class.java)
		}
		
		val coordinate = realm.schema.create(Coordinate.TABLE_NAME)
		coordinate.apply {
			addField(Coordinate.COORDINATE_LATITUDE, Double::class.java)
			addField(Coordinate.COORDINATE_LONGITUDE, Double::class.java)
			addField(Coordinate.COORDINATE_ALTITUDE, Double::class.java)
			addField(Coordinate.COORDINATE_CREATED_AT, Date::class.java)
		}
		
		val tracking = realm.schema.create(Tracking.TABLE_NAME)
		tracking.apply {
			addField(Tracking.TRACKING_ID, Int::class.java, FieldAttribute.PRIMARY_KEY)
			addField(Tracking.TRACKING_START_AT, Date::class.java)
					.setNullable(Tracking.TRACKING_START_AT, false)
			addField(Tracking.TRACKING_STOP_AT, Date::class.java)
			addRealmListField(Tracking.TRACKING_POINTS, coordinate)
		}
		
		val trackingFile = realm.schema.create(TrackingFile.TABLE_NAME)
		trackingFile.apply {
			addField(TrackingFile.FIELD_ID, Int::class.java, FieldAttribute.PRIMARY_KEY)
			addField(TrackingFile.FIELD_RESPONSE_ID, Int::class.java)
			addField(TrackingFile.FIELD_RESPONSE_SERVER_ID, String::class.java)
			addField(TrackingFile.FIELD_LOCAL_PATH, String::class.java)
					.setNullable(TrackingFile.FIELD_LOCAL_PATH, false)
			addField(TrackingFile.FIELD_REMOTE_PATH, String::class.java)
			addField(TrackingFile.FIELD_SYNC_STATE, Int::class.java)
			addField(TrackingFile.FIELD_STREAM_ID, Int::class.java)
			addField(TrackingFile.FIELD_STREAM_SERVER_ID, String::class.java)
		}
	}
	
	private fun migrateToV16(realm: DynamicRealm) {
		val voice = realm.schema.create(Voice.TABLE_NAME)
		voice.apply {
			addField(Voice.FIELD_ID, Int::class.java, FieldAttribute.PRIMARY_KEY)
			addField(Voice.FIELD_RESPONSE_ID, Int::class.java)
			addField(Voice.FIELD_RESPONSE_SERVER_ID, String::class.java)
			addField(Voice.FIELD_LOCAL_PATH, String::class.java)
			addField(Voice.FIELD_REMOTE_PATH, String::class.java)
			addField(Voice.FIELD_SYNC_STATE, Int::class.java)
		}
	}
	
	override fun hashCode(): Int {
		return 1
	}
	
	override fun equals(other: Any?): Boolean {
		return other.hashCode() == hashCode()
	}
}
