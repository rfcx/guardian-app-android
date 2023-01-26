package org.rfcx.incidents.domain.guardian.socket

import org.rfcx.incidents.data.interfaces.guardian.socket.AdminSocketRepository
import org.rfcx.incidents.data.interfaces.guardian.socket.GuardianSocketRepository
import org.rfcx.incidents.domain.base.NoResultUseCase

class CloseSocketUseCase(private val guardianRepository: GuardianSocketRepository, private val adminRepository: AdminSocketRepository) :
    NoResultUseCase() {
    override fun performAction() {
        guardianRepository.close()
        adminRepository.close()
    }
}
