package org.rfcx.incidents.data.local.guardian

import io.realm.Realm
import io.realm.kotlin.deleteFromRealm
import org.rfcx.incidents.entity.guardian.GuardianFile

class GuardianFileDb(private val realm: Realm) {
    suspend fun getAll(): List<GuardianFile> {
        val files = realm.where(GuardianFile::class.java).findAll()
        return realm.copyFromRealm(files)
    }

    suspend fun save(file: GuardianFile) {
        realm.executeTransaction {
            it.insertOrUpdate(file)
        }
    }

    suspend fun delete(file: GuardianFile) {
        realm.executeTransaction {
            val localFile = realm.where(GuardianFile::class.java).equalTo("name", file.name).findFirst()
            localFile?.deleteFromRealm()
        }
    }
}
