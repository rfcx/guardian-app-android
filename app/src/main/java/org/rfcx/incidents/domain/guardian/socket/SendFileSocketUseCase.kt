package org.rfcx.incidents.domain.guardian.socket

import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.socket.FileSocketRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.base.FlowWithParamUseCase
import org.rfcx.incidents.entity.guardian.file.GuardianFile
import org.rfcx.incidents.entity.guardian.file.GuardianFileSendStatus

class SendFileSocketUseCase(
    private val fileSocketRepository: FileSocketRepository
) : FlowWithParamUseCase<SendFileSocketParams, Result<GuardianFileSendStatus>>() {
    override fun performAction(param: SendFileSocketParams): Flow<Result<GuardianFileSendStatus>> {
        return fileSocketRepository.sendFile(param.file)
    }
}

data class SendFileSocketParams(
    val file: GuardianFile
)
