package org.rfcx.incidents.entity.guardian

data class GuardianFileItem(
    val file: GuardianFile, val status: FileStatus
)

enum class FileStatus { NOT_DOWNLOADED, UP_TO_DATE, NEED_UPDATE }
