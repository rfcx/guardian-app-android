package org.rfcx.incidents.domain.guardian.socket

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.guardian.socket.GuardianSocketRepositoryImpl
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.base.FlowUseCase

class InitSocketUseCase(private val repository: GuardianSocketRepositoryImpl) : FlowUseCase<Result<Boolean>>() {
    override fun performAction(): Flow<Result<Boolean>> {
        return repository.initialize()
    }
}
