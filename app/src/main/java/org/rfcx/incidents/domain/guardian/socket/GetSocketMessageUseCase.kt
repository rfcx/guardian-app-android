package org.rfcx.incidents.domain.guardian.socket

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.guardian.socket.GuardianSocketRepositoryImpl
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.base.FlowUseCase

class GetSocketMessageUseCase(private val repository: GuardianSocketRepositoryImpl) : FlowUseCase<Result<String>>() {
    override fun performAction(): Flow<Result<String>> {
        return repository.getMessage()
    }
}
