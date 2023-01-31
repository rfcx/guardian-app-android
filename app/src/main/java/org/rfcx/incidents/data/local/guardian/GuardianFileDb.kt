package org.rfcx.incidents.data.local.guardian

import io.realm.Realm
import io.realm.kotlin.deleteFromRealm
import io.realm.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.entity.guardian.GuardianFile

class GuardianFileDb(private val realm: Realm) {
    fun getAll(): List<GuardianFile> {
        val files = realm.where(GuardianFile::class.java).findAll()
        return realm.copyFromRealm(files)
    }

    fun getAllAsync(): Flow<List<GuardianFile>> {
        val files = realm.where(GuardianFile::class.java).findAllAsync().toFlow()
        return files
    }

    fun save(file: GuardianFile) {
        realm.executeTransaction {
            it.insertOrUpdate(file)
        }
    }

    fun delete(file: GuardianFile) {
        realm.executeTransaction {
            val localFile = realm.where(GuardianFile::class.java).equalTo("name", file.name).findFirst()
            localFile?.deleteFromRealm()
        }
    }
}
