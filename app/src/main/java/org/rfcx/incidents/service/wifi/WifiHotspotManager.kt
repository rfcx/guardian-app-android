package org.rfcx.incidents.service.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.util.wifi.WifiHotspotUtils

@OptIn(ExperimentalCoroutinesApi::class)
class WifiHotspotManager(private val context: Context) {

    companion object {
        private const val SSID_PREFIX = "rfcx"
    }

    private var wifiManager: WifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private lateinit var connectivityManager: ConnectivityManager

    fun nearbyHotspot(): Flow<Result<List<ScanResult>>> {

        wifiManager.startScan()
        return callbackFlow {
            trySendBlocking(Result.Loading)

            // No hotspot found timeout
            val delayJob = launch {
                delay(15000)
                trySendBlocking(Result.Success(listOf()))
            }

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent) {
                    if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                        val scanResult = wifiManager.scanResults
                        val guardianWifiHotspot = scanResult.filter {
                            it.SSID.contains(SSID_PREFIX)
                        }
                        if (guardianWifiHotspot.isNotEmpty()) {
                            delayJob.cancel()
                            trySendBlocking(Result.Success(guardianWifiHotspot))
                                .onFailure { throwable ->
                                    throwable?.let {
                                        Result.Error(it)
                                    }
                                }
                        }
                    }
                }
            }
            context.registerReceiver(
                receiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            )

            awaitClose {
                context.unregisterReceiver(receiver)
            }
        }
    }

    fun connectTo(guardian: ScanResult): Flow<Result<Boolean>> {
        // check if current wifi is connected to targeted hotspot
        if (WifiHotspotUtils.isConnectedWithGuardian(context, guardian.SSID)) {
            return flow { emit(Result.Success(true)) }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return callbackFlow<Result<Boolean>> {
                trySendBlocking(Result.Loading)

                // hotspot connection timeout
                val delayJob = launch {
                    delay(15000)
                    trySendBlocking(Result.Success(false))
                }

                val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder().also {
                    it.setSsid(guardian.SSID)
                    it.setWpa2Passphrase("rfcxrfcx")
                }.build()

                val networkRequest = NetworkRequest.Builder().also {
                    it.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    it.setNetworkSpecifier(wifiNetworkSpecifier)
                }.build()

                connectivityManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                connectivityManager.requestNetwork(
                    networkRequest,
                    object : ConnectivityManager.NetworkCallback() {
                        override fun onAvailable(network: Network) {
                            super.onAvailable(network)
                            connectivityManager.bindProcessToNetwork(network)
                            delayJob.cancel()
                            trySendBlocking(Result.Success(true))
                        }

                        override fun onLost(network: Network) {
                            super.onLost(network)
                            disconnect()
                            delayJob.cancel()
                            trySendBlocking(Result.Error(Throwable("onLost")))
                        }

                        override fun onUnavailable() {
                            super.onUnavailable()
                            delayJob.cancel()
                            trySendBlocking(Result.Error(Throwable("onUnavailable")))
                        }
                    }
                )
                awaitClose {
                    disconnect()
                }
            }
        } else {
            val wifiConfig = WifiConfiguration()
            wifiConfig.SSID = "\"${guardian.SSID}\""
            wifiConfig.preSharedKey = "\"rfcxrfcx\""
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
            wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA)

            wifiManager.disconnect()
            val netId = wifiManager.addNetwork(wifiConfig)
            wifiManager.enableNetwork(netId, true)
            wifiManager.reconnect()
            return callbackFlow<Result<Boolean>> {
                trySendBlocking(Result.Loading)

                // hotspot connection timeout
                val delayJob = launch {
                    delay(15000)
                    trySendBlocking(Result.Success(false))
                }

                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        val conManager =
                            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        val netInfo = conManager.activeNetworkInfo
                        if (netInfo != null && netInfo.isConnected && netInfo.type == ConnectivityManager.TYPE_WIFI) {
                            if (WifiHotspotUtils.isConnectedWithGuardian(context, guardian.SSID)) {
                                delayJob.cancel()
                                trySendBlocking(Result.Success(true))
                            }
                        }
                    }
                }
                context.registerReceiver(
                    receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                )

                awaitClose {
                    context.unregisterReceiver(receiver)
                }
            }
        }
    }

    fun disconnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (::connectivityManager.isInitialized) connectivityManager.bindProcessToNetwork(null)
        }
    }
}
