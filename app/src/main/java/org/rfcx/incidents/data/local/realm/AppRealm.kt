package org.rfcx.incidents.data.local.realm

import android.content.Context
import android.util.Log
import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmMigration
import io.realm.exceptions.RealmMigrationNeededException
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.entity.guardian.deployment.Deployment
import org.rfcx.incidents.entity.guardian.file.GuardianFile
import org.rfcx.incidents.entity.guardian.image.DeploymentImage
import org.rfcx.incidents.entity.guardian.registration.GuardianRegistration
import org.rfcx.incidents.entity.response.Asset
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.stream.Incident
import org.rfcx.incidents.entity.stream.Project
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.entity.stream.User
import java.util.Date

class AppRealm {

    companion object {
        private const val schemaVersion = 27L

        fun init(context: Context) {
            Realm.init(context)

            // Attempt to open and close realm to verify migrations
            var realmNeedsFallback = false
            try {
                val realm = Realm.getInstance(configuration())
                realm.close()
            } catch (e: RealmMigrationNeededException) {
                Log.e(Companion::class.java.name, "Realm migrations failed unexpectedly: ${e.message}")
                realmNeedsFallback = true
            }

            // Fallback for release (delete realm on error)
            if (realmNeedsFallback && !BuildConfig.DEBUG) {
                Log.e(Companion::class.java.name, "Falling back to complete Realm delete")
                try {
                    val realm = Realm.getInstance(fallbackConfiguration())
                    realm.close()
                } catch (_: RealmMigrationNeededException) {
                }
            }

            Realm.setDefaultConfiguration(configuration())
        }

        fun configuration(): RealmConfiguration {
            return RealmConfiguration.Builder().apply {
                schemaVersion(schemaVersion)
                migration(Migrations())
            }.allowWritesOnUiThread(true).build()
        }

        private fun fallbackConfiguration(): RealmConfiguration {
            return RealmConfiguration.Builder().apply {
                schemaVersion(schemaVersion)
                deleteRealmIfMigrationNeeded()
            }.allowWritesOnUiThread(true).build()
        }
    }
}

private class Migrations : RealmMigration {

    override fun migrate(c: DynamicRealm, oldVersion: Long, newVersion: Long) {
        if (oldVersion < 20L && newVersion >= 20) {
            migrateToV20(c)
        }

        if (oldVersion < 21L && newVersion >= 21) {
            migrateToV21(c)
        }

        if (oldVersion < 22L && newVersion >= 22) {
            migrateToV22(c)
        }

        if (oldVersion < 23L && newVersion >= 23) {
            migrateToV23(c)
        }

        if (oldVersion < 24L && newVersion >= 24) {
            migrateToV24(c)
        }

        if (oldVersion < 25L && newVersion >= 25) {
            migrateToV25(c)
        }

        if (oldVersion < 26L && newVersion >= 26) {
            migrateToV26(c)
        }

        if (oldVersion < 27L && newVersion >= 27) {
            migrateToV27(c)
        }
    }

    private fun migrateToV20(realm: DynamicRealm) {
        val stream = realm.schema.get(Stream.TABLE_NAME)
        stream?.apply {
            renameField("timezone", Stream.TAG_TIMEZONE_RAW)
        }

        val asset = realm.schema.create(Asset.TABLE_NAME)
        asset?.apply {
            addField(Asset.ASSET_ID, Int::class.java, FieldAttribute.PRIMARY_KEY)
            addField(Asset.ASSET_TYPE_RAW, String::class.java).setNullable(Asset.ASSET_TYPE_RAW, false)
            addField(Asset.ASSET_SERVER_ID, String::class.java)
            addField(Asset.ASSET_CREATED_AT, Date::class.java).setNullable(Asset.ASSET_CREATED_AT, false)
            addField(Asset.ASSET_LOCAL_PATH, String::class.java).setNullable(Asset.ASSET_LOCAL_PATH, false)
            addField(Asset.ASSET_SYNC_STATE, Int::class.java)
            addField(Asset.ASSET_REMOTE_PATH, String::class.java)
        }
        val response = realm.schema.get(Response.TABLE_NAME)
        response?.apply {
            addRealmListField(Response.RESPONSE_ASSETS, asset)
        }
    }

    private fun migrateToV21(realm: DynamicRealm) {
        val user = realm.schema.create(User.TABLE_NAME)
        user?.apply {
            addField(User.FIRSTNAME, String::class.java)
        }

        val incident = realm.schema.get(Incident.TABLE_NAME)
        incident?.apply {
            addRealmListField(Incident.FIELD_RESPONSES, user)
        }
    }

    private fun migrateToV22(realm: DynamicRealm) {
        val response = realm.schema.get(Response.TABLE_NAME)
        response?.apply {
            addField(Response.RESPONSE_IS_UNEXPECTED, Boolean::class.java)
            val guardianFile = realm.schema.create(GuardianFile.TABLE)
            guardianFile?.apply {
                addField(GuardianFile.FIELD_ID, String::class.java, FieldAttribute.PRIMARY_KEY).setRequired(
                    GuardianFile.FIELD_ID, true
                )
                addField(GuardianFile.FIELD_NAME, String::class.java).setRequired(GuardianFile.FIELD_NAME, true)
                addField(GuardianFile.FIELD_VERSION, String::class.java).setRequired(GuardianFile.FIELD_VERSION, true)
                addField(GuardianFile.FIELD_PATH, String::class.java).setRequired(GuardianFile.FIELD_PATH, true)
                addField(GuardianFile.FIELD_TYPE, String::class.java).setRequired(GuardianFile.FIELD_TYPE, true)
                addField(GuardianFile.FIELD_META, String::class.java).setRequired(GuardianFile.FIELD_META, true)
            }
        }
    }

    private fun migrateToV23(realm: DynamicRealm) {
        val project = realm.schema.get(Project.TABLE_NAME)
        project?.apply {
            addField(Project.PROJECT_OFFTIMES, String::class.java).setRequired(Project.PROJECT_OFFTIMES, true)
        }
    }

    private fun migrateToV24(realm: DynamicRealm) {
        val registration = realm.schema.create(GuardianRegistration.TABLE_NAME)
        registration?.apply {
            addField(GuardianRegistration.FIELD_GUID, String::class.java, FieldAttribute.PRIMARY_KEY).setRequired(GuardianRegistration.FIELD_GUID, true)
            addField(GuardianRegistration.FIELD_TOKEN, String::class.java).setRequired(GuardianRegistration.FIELD_TOKEN, true)
            addField(GuardianRegistration.FIELD_KEYSTORE_PASSPHRASE, String::class.java).setRequired(GuardianRegistration.FIELD_KEYSTORE_PASSPHRASE, true)
            addField(GuardianRegistration.FIELD_PIN_CODE, String::class.java).setRequired(GuardianRegistration.FIELD_PIN_CODE, true)
            addField(GuardianRegistration.FIELD_API_MQTT_HOST, String::class.java).setRequired(GuardianRegistration.FIELD_API_MQTT_HOST, true)
            addField(GuardianRegistration.FIELD_API_SMS_ADDRESS, String::class.java).setRequired(GuardianRegistration.FIELD_API_SMS_ADDRESS, true)
            addField(GuardianRegistration.FIELD_ENV, String::class.java).setRequired(GuardianRegistration.FIELD_ENV, true)
            addField(GuardianRegistration.FIELD_SYNC_STATE, Int::class.java)
            addField(GuardianRegistration.FIELD_CREATED_AT, Date::class.java).setRequired(GuardianRegistration.FIELD_CREATED_AT, true)
        }
    }

    private fun migrateToV25(realm: DynamicRealm) {
        val stream = realm.schema.get(Stream.TABLE_NAME)
        stream?.apply {
            addField(Stream.FIELD_ALTITUDE, Double::class.java)
            addField(Stream.FIELD_EXTERNAL_ID, String::class.java).setRequired(Stream.FIELD_EXTERNAL_ID, false)
            addField(Stream.FIELD_SYNC_STATE, Int::class.java)
        }
    }

    private fun migrateToV26(realm: DynamicRealm) {
        val image = realm.schema.create(DeploymentImage.TABLE_NAME)
        image?.apply {
            addField(DeploymentImage.FIELD_ID, Int::class.java, FieldAttribute.PRIMARY_KEY)
            addField(DeploymentImage.FIELD_LOCAL_PATH, String::class.java).setRequired(DeploymentImage.FIELD_LOCAL_PATH, true)
            addField(DeploymentImage.FIELD_REMOTE_PATH, String::class.java)
            addField(DeploymentImage.FIELD_IMAGE_LABEL, String::class.java).setRequired(DeploymentImage.FIELD_IMAGE_LABEL, true)
            addField(DeploymentImage.FIELD_SYNC_STATE, Int::class.java)
            addField(DeploymentImage.FIELD_CREATE_AT, Date::class.java).setRequired(DeploymentImage.FIELD_CREATE_AT, true)
        }
    }

    private fun migrateToV27(realm: DynamicRealm) {
        val deployment = realm.schema.create(Deployment.TABLE_NAME)
        val stream = realm.schema.get(Stream.TABLE_NAME)
        val image = realm.schema.get(DeploymentImage.TABLE_NAME)
        deployment?.apply {
            addField(Deployment.FIELD_ID, Int::class.java, FieldAttribute.PRIMARY_KEY)
            addField(Deployment.FIELD_EXTERNAL_ID, String::class.java)
            addField(Deployment.FIELD_DEPLOYED_AT, Date::class.java).setRequired(Deployment.FIELD_DEPLOYED_AT, true)
            addField(Deployment.FIELD_CREATED_AT, Date::class.java).setRequired(Deployment.FIELD_CREATED_AT, true)
            addField(Deployment.FIELD_DEPLOYMENT_KEY, String::class.java)
            addField(Deployment.FIELD_SYNC_STATE, Int::class.java)
            addField(Deployment.FIELD_IS_ACTIVE, Boolean::class.java)
            addField(Deployment.FIELD_DEVICE_PARAMETERS, String::class.java)
            if (image != null) {
                addRealmObjectField(Deployment.FIELD_IMAGES, image)
            }
        }

        stream?.apply {
            addRealmObjectField(Stream.FIELD_DEPLOYMENT, deployment)
        }
    }

    override fun hashCode(): Int {
        return 1
    }

    override fun equals(other: Any?): Boolean {
        return other.hashCode() == hashCode()
    }
}
