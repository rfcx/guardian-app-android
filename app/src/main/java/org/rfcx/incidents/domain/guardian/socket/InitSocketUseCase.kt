package org.rfcx.incidents.domain.guardian.socket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.zip
import org.rfcx.incidents.data.interfaces.guardian.socket.AdminSocketRepository
import org.rfcx.incidents.data.interfaces.guardian.socket.GuardianSocketRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.base.FlowUseCase

class InitSocketUseCase(private val guardianRepository: GuardianSocketRepository, private val adminRepository: AdminSocketRepository) :
    FlowUseCase<Result<Boolean>>() {
    override fun performAction(): Flow<Result<Boolean>> {
        return guardianRepository.initialize().combine(adminRepository.initialize()) { f1, f2 ->
            if ((f1 is Result.Success && f1.data == true) && (f2 is Result.Success && f2.data == true)) {
                Result.Success(true)
            } else {
                Result.Success(false)
            }
        }
    }
}
