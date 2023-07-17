package org.rfcx.incidents.domain.guardian.wifi

import org.rfcx.incidents.data.interfaces.guardian.wifi.WifiHotspotRepository
import org.rfcx.incidents.domain.base.NoResultUseCase

class DisconnectHotspotUseCase(private val repository: WifiHotspotRepository) : NoResultUseCase() {
    override fun performAction() {
        repository.disconnect()
    }
}
