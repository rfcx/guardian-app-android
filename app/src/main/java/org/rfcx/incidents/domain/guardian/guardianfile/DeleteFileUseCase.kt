package org.rfcx.incidents.domain.guardian.guardianfile

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.software.GuardianFileRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.base.FlowWithParamUseCase
import org.rfcx.incidents.entity.guardian.GuardianFile

class DeleteFileUseCase(private val repository: GuardianFileRepository) : FlowWithParamUseCase<DeleteFileParams, Result<Boolean>>() {
    override fun performAction(param: DeleteFileParams): Flow<Result<Boolean>> {
        return repository.delete(param.targetFile)
    }
}

data class DeleteFileParams(
    val targetFile: GuardianFile
)
