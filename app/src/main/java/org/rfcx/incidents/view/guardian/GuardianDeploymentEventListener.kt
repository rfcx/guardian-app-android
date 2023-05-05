package org.rfcx.incidents.view.guardian

import android.location.Location
import android.net.wifi.ScanResult
import kotlinx.coroutines.flow.SharedFlow
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.entity.stream.Stream

interface GuardianDeploymentEventListener {
    fun setupToolbar()
    fun showToolbar()
    fun hideToolbar()
    fun setToolbarTitle(title: String)

    fun changeScreen(screen: GuardianScreen)
    fun setPassedScreen(screen: GuardianScreen)
    fun back()
    fun next()
    fun goToSiteSetScreen(stream: Stream, isNewSite: Boolean)
    fun getPassedScreen(): List<GuardianScreen>

    fun connectHotspot(hotspot: ScanResult?)
    fun getHotspotConnectionState(): SharedFlow<Result<Boolean>>
    fun initSocket()
    fun sendHeartBeatSocket()
    fun getInitSocketState(): SharedFlow<Result<Boolean>>
    fun getSocketMessageState(): SharedFlow<Result<List<String>>>
    fun closeSocket()
}
