package org.rfcx.incidents.domain.guardian.registration

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.GuardianRegistrationRepository
import org.rfcx.incidents.domain.base.FlowWithParamUseCase
import org.rfcx.incidents.entity.guardian.registration.GuardianRegistration

class SaveRegistrationUseCase(private val repository: GuardianRegistrationRepository) : FlowWithParamUseCase<OfflineRegistrationParams, Boolean>() {
    override fun performAction(param: OfflineRegistrationParams): Flow<Boolean> {
        return repository.saveRegistration(param.registration)
    }
}

data class OfflineRegistrationParams(
    val registration: GuardianRegistration
)
