package org.rfcx.incidents.entity.guardian

data class GuardianFile(
    val file: FileResponse,
    val status: FileStatus
)

interface FileResponse

enum class FileStatus { NOT_DOWNLOADED, UP_TO_DATE, NEED_UPDATE }
