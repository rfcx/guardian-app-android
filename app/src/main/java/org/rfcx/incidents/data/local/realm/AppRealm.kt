package org.rfcx.incidents.data.local.realm

import android.content.Context
import android.util.Log
import io.realm.DynamicRealm
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmMigration
import io.realm.exceptions.RealmMigrationNeededException
import org.rfcx.incidents.BuildConfig
import org.rfcx.incidents.entity.stream.Stream

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
