package org.rfcx.incidents.domain.guardian.socket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.rfcx.incidents.data.guardian.socket.GuardianSocketRepositoryImpl
import org.rfcx.incidents.data.interfaces.guardian.socket.AdminSocketRepository
import org.rfcx.incidents.data.interfaces.guardian.socket.GuardianSocketRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.base.FlowUseCase

class GetSocketMessageUseCase(private val guardianRepository: GuardianSocketRepository, private val adminRepository: AdminSocketRepository) : FlowUseCase<Result<List<String>>>() {
    override fun performAction(): Flow<Result<List<String>>> {
        return guardianRepository.getMessage().combine(adminRepository.getMessage()) { f1, f2 ->
            if (f1 is Result.Loading || f2 is Result.Loading) {
                return@combine Result.Loading
            }
            if ((f1 is Result.Success && f1.data.isNotEmpty()) && (f2 is Result.Success && f2.data.isNotEmpty())) {
                return@combine Result.Success(listOf(f1.data, f2.data))
            } else {
                return@combine Result.Success(listOf())
            }
        }
    }
}
