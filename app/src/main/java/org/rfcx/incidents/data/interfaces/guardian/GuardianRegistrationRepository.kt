package org.rfcx.incidents.data.interfaces.guardian

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.registration.GuardianRegisterResponse
import org.rfcx.incidents.entity.guardian.registration.GuardianRegisterRequest
import org.rfcx.incidents.entity.guardian.registration.GuardianRegistration

interface GuardianRegistrationRepository {
    fun saveRegistration(registration: GuardianRegistration): Flow<Boolean>
    fun sendRegistrationOnline(env: String, registration: GuardianRegisterRequest): Flow<Result<GuardianRegisterResponse>>
    fun list(): Flow<List<GuardianRegistration>>
}
