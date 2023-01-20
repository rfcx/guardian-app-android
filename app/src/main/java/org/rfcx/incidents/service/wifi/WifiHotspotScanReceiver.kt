package org.rfcx.incidents.service.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WifiHotspotScanReceiver(
    private val wifiManager: WifiManager,
    private val nearbyHotspotListener: NearbyHotspotListener
    ) : BroadcastReceiver() {

    companion object {
        private const val SSID_PREFIX = "rfcx"
    }

    private val scope = CoroutineScope(SupervisorJob())

    override fun onReceive(context: Context?, intent: Intent?) {
        val pendingResult = goAsync()
        scope.launch(Dispatchers.Default) {
            try {
                if (intent!!.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                    val scanResult = wifiManager.scanResults
                    val guardianWifiHotspot = scanResult.filter {
                        it.SSID.contains(SSID_PREFIX)
                    }
                    if (guardianWifiHotspot.isNotEmpty()) {
                        nearbyHotspotListener.onScanReceive(guardianWifiHotspot)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
