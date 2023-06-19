package org.rfcx.incidents.domain.guardian.registration

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.GuardianRegistrationRepository
import org.rfcx.incidents.domain.base.FlowUseCase
import org.rfcx.incidents.entity.guardian.registration.GuardianRegistration

class GetRegistrationUseCase(
    private val registrationRepository: GuardianRegistrationRepository
): FlowUseCase<List<GuardianRegistration>>() {
    override fun performAction(): Flow<List<GuardianRegistration>> {
        return registrationRepository.list()
    }
}
