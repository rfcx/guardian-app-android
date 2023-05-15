package org.rfcx.incidents.domain.guardian.guardianfile

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.software.GuardianFileRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.base.FlowWithParamUseCase
import org.rfcx.incidents.entity.guardian.file.GuardianFile

class DownloadFileUseCase(private val repository: GuardianFileRepository) : FlowWithParamUseCase<DownloadFileParams, Result<Boolean>>() {
    override fun performAction(param: DownloadFileParams): Flow<Result<Boolean>> {
        return repository.download(param.targetFile)
    }
}

data class DownloadFileParams(
    val targetFile: GuardianFile
)
