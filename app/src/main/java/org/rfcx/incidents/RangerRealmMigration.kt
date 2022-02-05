package org.rfcx.incidents

import io.realm.DynamicRealm
import io.realm.RealmMigration

@Suppress("DEPRECATION")
class RangerRealmMigration : RealmMigration {

    override fun migrate(c: DynamicRealm, oldVersion: Long, newVersion: Long) {}

    override fun hashCode(): Int {
        return 1
    }

    override fun equals(other: Any?): Boolean {
        return other.hashCode() == hashCode()
    }
}
