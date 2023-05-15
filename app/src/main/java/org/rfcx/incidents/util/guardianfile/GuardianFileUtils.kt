package org.rfcx.incidents.util.guardianfile

import org.rfcx.incidents.entity.guardian.file.FileStatus
import org.rfcx.incidents.entity.guardian.UpdateStatus

object GuardianFileUtils {
    fun compareIfNeedToDownload(version1: String?, version2: String?): FileStatus {
        if (version2 == null) {
            return FileStatus.NOT_DOWNLOADED
        }
        if (version1 == null) {
            return FileStatus.NO_INTERNET
        }
        val levels1 = version1.split(".")
        val levels2 = version2.split(".")
        val length = levels1.size.coerceAtLeast(levels2.size)
        for (i in 0 until length) {
            val v1 = if (i < levels1.size) levels1[i].toInt() else 0
            val v2 = if (i < levels2.size) levels2[i].toInt() else 0
            val compare = v1.compareTo(v2)
            if (compare < 0) {
                return FileStatus.NEED_DOWNLOAD
            }
        }
        return FileStatus.UP_TO_DATE
    }

    fun compareIfNeedToUpdate(installed: String?, downloaded: String?): UpdateStatus {
        if (downloaded == null) {
            return UpdateStatus.NOT_INSTALLED
        }
        if (installed == null) {
            return UpdateStatus.NOT_INSTALLED
        }
        val levels1 = installed.split(".")
        val levels2 = downloaded.split(".")
        val length = levels1.size.coerceAtLeast(levels2.size)
        for (i in 0 until length) {
            val v1 = if (i < levels1.size) levels1[i].toInt() else 0
            val v2 = if (i < levels2.size) levels2[i].toInt() else 0
            val compare = v1.compareTo(v2)
            if (compare < 0) {
                return UpdateStatus.NEED_UPDATE
            }
        }
        return UpdateStatus.UP_TO_DATE
    }

    fun calculateVersionValue(versionName: String): Int {
        return try {
            val majorVersion = versionName.substring(0, versionName.indexOf(".")).toInt()
            val subVersion =
                versionName.substring(1 + versionName.indexOf("."), versionName.lastIndexOf("."))
                    .toInt()
            val updateVersion = versionName.substring(1 + versionName.lastIndexOf(".")).toInt()
            10000 * majorVersion + 100 * subVersion + updateVersion
        } catch (e: Exception) {
            0
        }
    }
}
