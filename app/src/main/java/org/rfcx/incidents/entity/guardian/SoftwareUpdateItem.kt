package org.rfcx.incidents.entity.guardian

sealed class SoftwareUpdateItem {
    data class SoftwareUpdateHeader(val name: String) : SoftwareUpdateItem()
    data class SoftwareUpdateVersion(
        val parent: String,
        val updateFile: GuardianFile?,
        val installedVersion: String?,
        var status: UpdateStatus,
        var isEnabled: Boolean,
        val progress: Int?
    ) : SoftwareUpdateItem()
}

sealed class ClassifierUploadItem {
    data class ClassifierUploadHeader(val name: String) : ClassifierUploadItem()
    data class ClassifierUploadVersion(
        val parent: String,
        val updateFile: GuardianFile?,
        val installedVersion: String?,
        var status: UpdateStatus,
        var isEnabled: Boolean,
        var isActive: Boolean,
        val progress: Int?
    ) : ClassifierUploadItem()
}

enum class UpdateStatus { NOT_INSTALLED, LOADING, UP_TO_DATE, NEED_UPDATE }
