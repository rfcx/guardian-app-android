package org.rfcx.incidents.data.local.guardian

import io.realm.Realm
import org.rfcx.incidents.entity.guardian.GuardianFile
import org.rfcx.incidents.entity.guardian.GuardianFileItem
import org.rfcx.incidents.entity.stream.Stream

class GuardianFileDb(private val realm: Realm) {
    fun getAll(): List<GuardianFile> = realm.where(GuardianFile::class.java).findAll()
}
