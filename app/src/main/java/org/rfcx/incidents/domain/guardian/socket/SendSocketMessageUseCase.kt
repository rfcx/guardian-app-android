package org.rfcx.incidents.domain.guardian.socket

import org.rfcx.incidents.data.interfaces.guardian.socket.AdminSocketRepository
import org.rfcx.incidents.data.interfaces.guardian.socket.FileSocketRepository
import org.rfcx.incidents.data.interfaces.guardian.socket.GuardianSocketRepository
import org.rfcx.incidents.domain.base.NoResultWithParamUseCase
import org.rfcx.incidents.service.wifi.socket.BaseSocketManager

class SendSocketMessageUseCase(
    private val guardianRepository: GuardianSocketRepository,
    private val adminRepository: AdminSocketRepository,
    private val fileSocketRepository: FileSocketRepository
) : NoResultWithParamUseCase<SendMessageParams>() {
    override fun performAction(param: SendMessageParams) {
        when (param.type) {
            BaseSocketManager.Type.GUARDIAN -> guardianRepository.sendMessage(param.message)
            BaseSocketManager.Type.ADMIN -> adminRepository.sendMessage(param.message)
            BaseSocketManager.Type.FILE -> fileSocketRepository.sendMessage(param.message)
            BaseSocketManager.Type.ALL -> {
                guardianRepository.sendMessage(param.message)
                adminRepository.sendMessage(param.message)
            }
        }
    }
}

data class SendMessageParams(
    val type: BaseSocketManager.Type,
    val message: String
)
