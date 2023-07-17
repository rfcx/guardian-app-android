package org.rfcx.incidents.entity.guardian.registration

data class GuardianRegisterRequest(
    val guid: String,
    val token: String?,
    val pinCode: String?
)
