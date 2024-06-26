package org.rfcx.incidents.entity.guardian.file

data class GuardianFileItem(
    val remote: GuardianFile?,
    val local: GuardianFile?,
    val status: FileStatus
)

enum class FileStatus { NOT_DOWNLOADED, UP_TO_DATE, NEED_DOWNLOAD, NO_INTERNET }
