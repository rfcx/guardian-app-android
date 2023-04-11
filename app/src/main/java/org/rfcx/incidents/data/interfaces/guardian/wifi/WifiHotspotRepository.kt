package org.rfcx.incidents.data.interfaces.guardian.wifi

import android.net.wifi.ScanResult
import kotlinx.coroutines.flow.Flow
import org.rfcx.incidents.data.remote.common.Result

interface WifiHotspotRepository {
    fun getHotspots(): Flow<Result<List<ScanResult>>>
    fun connect(scanResult: ScanResult): Flow<Result<Boolean>>

    fun disconnect()
}
