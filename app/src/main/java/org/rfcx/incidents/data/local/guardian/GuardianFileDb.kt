package org.rfcx.incidents.data.local.guardian

import io.realm.Realm
import io.realm.kotlin.deleteFromRealm
import io.realm.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.entity.guardian.file.GuardianFile
import org.rfcx.incidents.entity.guardian.file.GuardianFileType

class GuardianFileDb(private val realm: Realm) {
    fun getSoftwareAllAsync(): Flow<List<GuardianFile>> {
        val files = realm.where(GuardianFile::class.java).equalTo(GuardianFile.FIELD_TYPE, GuardianFileType.SOFTWARE.value).findAllAsync().toFlow()
        return files
    }

    fun getClassifierAllAsync(): Flow<List<GuardianFile>> {
        val files = realm.where(GuardianFile::class.java).equalTo(GuardianFile.FIELD_TYPE, GuardianFileType.CLASSIFIER.value).findAllAsync().toFlow()
        return files
    }

    fun save(file: GuardianFile) {
        realm.executeTransaction {
            it.insertOrUpdate(file)
        }
    }

    fun delete(file: GuardianFile) {
        realm.executeTransaction {
            val localFile = realm.where(GuardianFile::class.java).equalTo(GuardianFile.FIELD_NAME, file.name).findFirst()
            localFile?.deleteFromRealm()
        }
    }
}
