package org.rfcx.incidents.util.guardianfile

import org.rfcx.incidents.entity.guardian.FileStatus

object GuardianFileUtils {
    fun compareIfNeedToUpdate(version1: String, version2: String?): FileStatus {
        if (version2 == null) {
            return FileStatus.NOT_DOWNLOADED
        }
        val levels1 = version1.split(".")
        val levels2 = version2.split(".")
        val length = levels1.size.coerceAtLeast(levels2.size)
        for (i in 0 until length) {
            val v1 = if (i < levels1.size) levels1[i].toInt() else 0
            val v2 = if (i < levels2.size) levels2[i].toInt() else 0
            val compare = v1.compareTo(v2)
            if (compare < 0) {
                return FileStatus.NEED_UPDATE
            }
        }
        return FileStatus.UP_TO_DATE
    }
}
