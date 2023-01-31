package org.rfcx.incidents.domain.guardian.software

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.software.SoftwareRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.software.SoftwareResponse
import org.rfcx.incidents.domain.base.FlowUseCase
import org.rfcx.incidents.entity.guardian.GuardianFile

class GetSoftwareLocalUseCase(private val repository: SoftwareRepository) : FlowUseCase<List<GuardianFile>>() {
    override fun performAction(): Flow<List<GuardianFile>> {
        return repository.getLocalAsFlow()
    }
}
