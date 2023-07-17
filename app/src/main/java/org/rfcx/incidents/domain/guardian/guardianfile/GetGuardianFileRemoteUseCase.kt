package org.rfcx.incidents.domain.guardian.guardianfile

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.software.GuardianFileRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.software.GuardianFileResponse
import org.rfcx.incidents.domain.base.FlowWithParamUseCase
import org.rfcx.incidents.entity.guardian.file.GuardianFileType

class GetGuardianFileRemoteUseCase(private val repository: GuardianFileRepository) :
    FlowWithParamUseCase<GetGuardianFileParams, Result<List<GuardianFileResponse>>>() {
    override fun performAction(param: GetGuardianFileParams): Flow<Result<List<GuardianFileResponse>>> {
        return when (param.type) {
            GuardianFileType.SOFTWARE -> repository.getSoftwareRemote()
            GuardianFileType.CLASSIFIER -> repository.getClassifierRemote()
        }
    }
}

data class GetGuardianFileParams(
    val type: GuardianFileType
)
