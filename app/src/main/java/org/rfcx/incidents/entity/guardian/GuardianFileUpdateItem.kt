package org.rfcx.incidents.entity.guardian

sealed class GuardianFileUpdateItem {
    data class GuardianFileUpdateHeader(val name: String) : GuardianFileUpdateItem()
    data class GuardianFileUpdateVersion(val parent: String, val updateFile: GuardianFile?, val installedVersion: String?, val status: UpdateStatus, val progress: Int?) : GuardianFileUpdateItem()
}

enum class UpdateStatus { NOT_DOWNLOADED, WAITING, LOADING, UP_TO_DATE, NEED_UPDATE }
