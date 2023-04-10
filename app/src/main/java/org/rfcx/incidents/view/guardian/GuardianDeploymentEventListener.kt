package org.rfcx.incidents.view.guardian

import kotlinx.coroutines.flow.SharedFlow
import org.rfcx.incidents.data.remote.common.Result

interface GuardianDeploymentEventListener {
    fun setupToolbar()
    fun showToolbar()
    fun hideToolbar()
    fun setToolbarTitle(title: String)

    fun changeScreen(screen: GuardianScreen)
    fun setPassedScreen(screen: GuardianScreen)
    fun back()
    fun next()
    fun getPassedScreen(): List<GuardianScreen>

    fun initSocket()
    fun sendHeartBeatSocket()
    fun getInitSocketState(): SharedFlow<Result<Boolean>>
    fun getSocketMessageState(): SharedFlow<Result<List<String>>>
    fun closeSocket()
}
