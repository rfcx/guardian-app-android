package org.rfcx.incidents.domain.guardian.guardianfile

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.software.GuardianFileRepository
import org.rfcx.incidents.domain.base.FlowWithParamUseCase
import org.rfcx.incidents.entity.guardian.GuardianFile
import org.rfcx.incidents.entity.guardian.GuardianFileType

class GetGuardianFileLocalUseCase(private val repository: GuardianFileRepository) : FlowWithParamUseCase<GetGuardianFileLocalParams, List<GuardianFile>>() {
    override fun performAction(param: GetGuardianFileLocalParams): Flow<List<GuardianFile>> {
        return when(param.type) {
            GuardianFileType.SOFTWARE -> repository.getSoftwareLocalAsFlow()
            GuardianFileType.CLASSIFIER -> repository.getClassifierLocalAsFlow()
        }
    }
}

data class GetGuardianFileLocalParams(
    val type: GuardianFileType
)
