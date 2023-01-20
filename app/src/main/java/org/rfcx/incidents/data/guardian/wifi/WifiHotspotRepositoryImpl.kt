package org.rfcx.incidents.data.guardian.wifi

import android.net.wifi.ScanResult
import org.rfcx.incidents.data.interfaces.guardian.wifi.WifiHotspotRepository
import org.rfcx.incidents.service.wifi.NearbyHotspotListener
import org.rfcx.incidents.service.wifi.WifiHotspotManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class WifiHotspotRepositoryImpl(
    private val wifiHotspotManager: WifiHotspotManager
    ) : WifiHotspotRepository {
    override suspend fun getHotspots(): List<ScanResult> {
        return suspendCoroutine {
            wifiHotspotManager.nearbyHotspot(object: NearbyHotspotListener{
                override fun onScanReceive(result: List<ScanResult>) {
                    it.resume(result)
                }
            })
        }
    }
}
