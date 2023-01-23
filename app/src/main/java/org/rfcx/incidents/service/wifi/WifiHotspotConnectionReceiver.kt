package org.rfcx.incidents.service.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import org.rfcx.incidents.util.wifi.WifiHotspotUtils

class WifiHotspotConnectionReceiver(private val targetHotspot: String, private val nearbyHotspotListener: NearbyHotspotListener) :
    BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // val conManager =
        //     context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // val netInfo = conManager.activeNetworkInfo
        // if (netInfo != null && netInfo.isConnected && netInfo.type == ConnectivityManager.TYPE_WIFI) {
        //     if (WifiHotspotUtils.isConnectedWithGuardian(context, targetHotspot)) {
        //         nearbyHotspotListener.onWifiConnected()
        //     }
        // }
    }
}
