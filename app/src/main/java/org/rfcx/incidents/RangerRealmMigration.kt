package org.rfcx.incidents

import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration
import org.rfcx.incidents.entity.Classification
import org.rfcx.incidents.entity.Incident
import org.rfcx.incidents.entity.Stream
import org.rfcx.incidents.entity.alert.Alert
import org.rfcx.incidents.entity.location.Coordinate
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.entity.location.TrackingFile
import org.rfcx.incidents.entity.project.Project
import org.rfcx.incidents.entity.report.ReportImage
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.response.Voice
import java.util.*

@Suppress("DEPRECATION")
class RangerRealmMigration : RealmMigration {

    override fun migrate(c: DynamicRealm, oldVersion: Long, newVersion: Long) {
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
        if (oldVersion < 17L && newVersion >= 17L) {
            migrateToV17(c)
        }
        if (oldVersion < 18L && newVersion >= 18L) {
            migrateToV18(c)
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

    private fun migrateToV17(realm: DynamicRealm) {
        val response = realm.schema.get(Response.TABLE_NAME)
        response?.apply {
            addField(Response.RESPONSE_POACHING_SCALE, Int::class.java)
            addRealmListField(Response.RESPONSE_POACHING_EVIDENCE, Int::class.java)
                .setRequired(Response.RESPONSE_POACHING_EVIDENCE, false)
            addRealmListField(Response.RESPONSE_INVESTIGATE_TYPE, Int::class.java)
                .setRequired(Response.RESPONSE_INVESTIGATE_TYPE, false)
        }
    }

    private fun migrateToV18(realm: DynamicRealm) {
        val stream = realm.schema.get(Stream.TABLE_NAME)
        stream?.apply {
            addField(Stream.STREAM_INCIDENT_REF, Int::class.java)
        }
    }

    override fun hashCode(): Int {
        return 1
    }

    override fun equals(other: Any?): Boolean {
        return other.hashCode() == hashCode()
    }
}
