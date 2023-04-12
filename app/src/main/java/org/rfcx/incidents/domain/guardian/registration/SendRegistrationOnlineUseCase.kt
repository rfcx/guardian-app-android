package org.rfcx.incidents.domain.guardian.registration

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.GuardianRegistrationRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.registration.GuardianRegisterResponse
import org.rfcx.incidents.domain.base.FlowWithParamUseCase
import org.rfcx.incidents.entity.guardian.registration.GuardianRegisterRequest

class SendRegistrationOnlineUseCase(private val repository: GuardianRegistrationRepository) : FlowWithParamUseCase<OnlineRegistrationParams, Result<GuardianRegisterResponse>>() {
    override fun performAction(param: OnlineRegistrationParams): Flow<Result<GuardianRegisterResponse>> {
        return repository.sendRegistrationOnline(param.env, param.registration)
    }
}

data class OnlineRegistrationParams(
    val env: String,
    val registration: GuardianRegisterRequest
)
