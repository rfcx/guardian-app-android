package org.rfcx.incidents.data.guardian.wifi

import android.net.wifi.ScanResult
import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.interfaces.guardian.wifi.WifiHotspotRepository
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.service.wifi.WifiHotspotManager

class WifiHotspotRepositoryImpl(
    private val wifiHotspotManager: WifiHotspotManager
    ) : WifiHotspotRepository {
    override fun getHotspots(): Flow<Result<List<ScanResult>>> {
        return wifiHotspotManager.nearbyHotspot()
    }

    override fun connect(scanResult: ScanResult): Flow<Result<Boolean>> {
        return wifiHotspotManager.connectTo(scanResult)
    }
}
