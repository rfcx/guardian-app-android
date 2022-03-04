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
import org.rfcx.incidents.entity.response.Asset
import org.rfcx.incidents.entity.response.Response
import org.rfcx.incidents.entity.stream.Stream
import java.util.Date

class AppRealm {

    companion object {
        private const val schemaVersion = 20L

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
    }

    private fun migrateToV20(realm: DynamicRealm) {
        val asset = realm.schema.create(Asset.TABLE_NAME)
        asset?.apply {
            addField(Asset.ASSET_ID, Int::class.java, FieldAttribute.PRIMARY_KEY)
            addField(Asset.ASSET_TYPE, String::class.java).setNullable(Asset.ASSET_TYPE, false)
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

        val stream = realm.schema.get(Stream.TABLE_NAME)
        stream?.apply {
            renameField("timezone", Stream.TAG_TIMEZONE_RAW)
        }
    }

    override fun hashCode(): Int {
        return 1
    }

    override fun equals(other: Any?): Boolean {
        return other.hashCode() == hashCode()
    }
}
