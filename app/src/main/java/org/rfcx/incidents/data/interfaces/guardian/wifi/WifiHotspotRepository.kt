package org.rfcx.incidents.data.interfaces.guardian.wifi

import android.net.wifi.ScanResult

interface WifiHotspotRepository {

    suspend fun getHotspots(): List<ScanResult>

}
