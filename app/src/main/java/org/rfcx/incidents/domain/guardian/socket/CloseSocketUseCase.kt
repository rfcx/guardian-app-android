package org.rfcx.incidents.domain.guardian.socket

import org.rfcx.incidents.data.interfaces.guardian.socket.AdminSocketRepository
import org.rfcx.incidents.data.interfaces.guardian.socket.AudioSocketRepository
import org.rfcx.incidents.data.interfaces.guardian.socket.FileSocketRepository
import org.rfcx.incidents.data.interfaces.guardian.socket.GuardianSocketRepository
import org.rfcx.incidents.domain.base.NoResultWithParamUseCase
import org.rfcx.incidents.service.wifi.socket.BaseSocketManager

class CloseSocketUseCase(
    private val guardianRepository: GuardianSocketRepository,
    private val adminRepository: AdminSocketRepository,
    private val fileSocketRepository: FileSocketRepository,
    private val audioSocketRepository: AudioSocketRepository
) :
    NoResultWithParamUseCase<CloseSocketParams>() {
    override fun performAction(param: CloseSocketParams) {
        when (param.type) {
            BaseSocketManager.Type.GUARDIAN -> guardianRepository.close()
            BaseSocketManager.Type.ADMIN -> adminRepository.close()
            BaseSocketManager.Type.FILE -> fileSocketRepository.close()
            BaseSocketManager.Type.AUDIO -> audioSocketRepository.close()
            BaseSocketManager.Type.ALL -> {
                guardianRepository.close()
                adminRepository.close()
                fileSocketRepository.close()
                audioSocketRepository.close()
            }
        }
    }
}

data class CloseSocketParams(
    val type: BaseSocketManager.Type
)
