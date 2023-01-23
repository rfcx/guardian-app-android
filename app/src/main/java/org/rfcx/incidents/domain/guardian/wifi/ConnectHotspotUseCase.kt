package org.rfcx.incidents.domain.guardian.wifi

import android.net.wifi.ScanResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.rfcx.incidents.data.interfaces.guardian.wifi.WifiHotspotRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.base.FlowWithParamUseCase

class ConnectHotspotUseCase(private val repository: WifiHotspotRepository) : FlowWithParamUseCase<ConnectHotspotParams, Result<Boolean>>() {

    override fun performAction(param: ConnectHotspotParams?): Flow<Result<Boolean>> {
        if (param == null) return flow { emit(Result.Error(Throwable("null parameter is not allowed"))) }
        if (param.targetHotspot == null) return flow { emit(Result.Error(Throwable("null target hotspot is not allowed"))) }
        return repository.connect(param.targetHotspot)
    }
}

data class ConnectHotspotParams(
    val targetHotspot: ScanResult?
)
