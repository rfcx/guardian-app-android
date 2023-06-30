package org.rfcx.incidents.view.guardian

import android.content.SharedPreferences
import android.net.wifi.ScanResult
import androidx.preference.Preference
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.view.guardian.checklist.photos.Image

interface GuardianDeploymentEventListener {
    fun setupToolbar()
    fun showToolbar()
    fun hideToolbar()
    fun showThreeDots()
    fun hideThreeDots()
    fun setToolbarTitle(title: String)

    fun isAbleToDeploy(): Boolean
    fun changeScreen(screen: GuardianScreen)
    fun setPassedScreen(screen: GuardianScreen)
    fun back()
    fun next()
    fun finishDeploy()
    fun goToSiteSetScreen(stream: Stream, isNewSite: Boolean)
    fun goToMapPickerScreen(stream: Stream)
    fun getPassedScreen(): List<GuardianScreen>
    fun nextWithStream(stream: Stream)

    fun connectHotspot(hotspot: ScanResult?)
    fun getHotspotConnectionState(): StateFlow<Result<Boolean>?>
    fun initSocket()
    fun sendHeartBeatSocket()
    fun getInitSocketState(): StateFlow<Result<Boolean>?>
    fun getSocketMessageState(): StateFlow<Result<List<String>>?>
    fun closeSocket()

    fun getSavedStream(): Stream
    fun getSavedImages(): List<Image>
    fun setSavedImages(images: List<Image>)

    fun setGuardianPrefs(prefs: List<Preference>)
    fun setChangedPrefs(prefs: String)
    fun getChangedPrefs(): String
    fun setEditor(editor: SharedPreferences.Editor?)
}
