package org.rfcx.incidents.domain.guardian.software

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.software.SoftwareRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.base.FlowWithParamUseCase
import org.rfcx.incidents.entity.guardian.GuardianFile

class DeleteFileUseCase(private val repository: SoftwareRepository) : FlowWithParamUseCase<DeleteFileParams, Result<Boolean>>() {
    override fun performAction(param: DeleteFileParams): Flow<Result<Boolean>> {
        return repository.delete(param.targetFile)
    }
}

data class DeleteFileParams(
    val targetFile: GuardianFile
)
