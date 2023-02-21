package org.rfcx.incidents.domain.guardian.socket

import org.rfcx.incidents.data.interfaces.guardian.socket.AdminSocketRepository
import org.rfcx.incidents.data.interfaces.guardian.socket.FileSocketRepository
import org.rfcx.incidents.data.interfaces.guardian.socket.GuardianSocketRepository
import org.rfcx.incidents.domain.base.NoResultWithParamUseCase
import org.rfcx.incidents.service.wifi.socket.BaseSocketMananger

class SendSocketMessageUseCase(private val guardianRepository: GuardianSocketRepository, private val adminRepository: AdminSocketRepository, private val fileSocketRepository: FileSocketRepository) :
    NoResultWithParamUseCase<SendMessageParams>() {
    override fun performAction(param: SendMessageParams) {
        when (param.type) {
            BaseSocketMananger.Type.GUARDIAN -> guardianRepository.sendMessage(param.message)
            BaseSocketMananger.Type.ADMIN -> adminRepository.sendMessage(param.message)
            BaseSocketMananger.Type.FILE -> fileSocketRepository.sendMessage(param.message)
            BaseSocketMananger.Type.ALL -> {
                guardianRepository.sendMessage(param.message)
                adminRepository.sendMessage(param.message)
            }
        }
    }
}

data class SendMessageParams(
    val type: BaseSocketMananger.Type, val message: String
)
