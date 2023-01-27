package org.rfcx.incidents.domain.guardian.software

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.software.SoftwareRepository
import org.rfcx.incidents.domain.base.FlowUseCase
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.software.SoftwareResponse

class GetSoftwareRemoteUseCase(private val repository: SoftwareRepository) : FlowUseCase<Result<List<SoftwareResponse>>>() {
    override fun performAction(): Flow<Result<List<SoftwareResponse>>> {
        return repository.getRemote()
    }
}
