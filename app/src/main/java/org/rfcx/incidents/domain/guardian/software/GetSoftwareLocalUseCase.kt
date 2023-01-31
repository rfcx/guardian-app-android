package org.rfcx.incidents.domain.guardian.software

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.software.GuardianFileRepository
import org.rfcx.incidents.domain.base.FlowUseCase
import org.rfcx.incidents.entity.guardian.GuardianFile

class GetSoftwareLocalUseCase(private val repository: GuardianFileRepository) : FlowUseCase<List<GuardianFile>>() {
    override fun performAction(): Flow<List<GuardianFile>> {
        return repository.getLocalAsFlow()
    }
}
