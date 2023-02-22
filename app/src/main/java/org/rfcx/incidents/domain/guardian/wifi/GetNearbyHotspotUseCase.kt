package org.rfcx.incidents.domain.guardian.wifi

import android.net.wifi.ScanResult
import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.wifi.WifiHotspotRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.base.FlowUseCase

class GetNearbyHotspotUseCase(private val repository: WifiHotspotRepository) : FlowUseCase<Result<List<ScanResult>>>() {
    override fun performAction(): Flow<Result<List<ScanResult>>> {
        return repository.getHotspots()
    }
}
