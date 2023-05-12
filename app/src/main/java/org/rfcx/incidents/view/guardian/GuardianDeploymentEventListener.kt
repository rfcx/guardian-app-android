package org.rfcx.incidents.view.guardian

import android.content.SharedPreferences
import android.location.Location
import android.net.wifi.ScanResult
import androidx.preference.Preference
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.SharedFlow
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

    fun changeScreen(screen: GuardianScreen)
    fun setPassedScreen(screen: GuardianScreen)
    fun back()
    fun next()
    fun goToSiteSetScreen(stream: Stream, isNewSite: Boolean)
    fun goToMapPickerScreen(stream: Stream)
    fun getPassedScreen(): List<GuardianScreen>
    fun nextWithStream(stream: Stream)

    fun connectHotspot(hotspot: ScanResult?)
    fun getHotspotConnectionState(): SharedFlow<Result<Boolean>>
    fun initSocket()
    fun sendHeartBeatSocket()
    fun getInitSocketState(): SharedFlow<Result<Boolean>>
    fun getSocketMessageState(): SharedFlow<Result<List<String>>>
    fun closeSocket()

    fun getSavedImages(): List<Image>
    fun setSavedImages(images: List<Image>)

    fun setGuardianPrefs(prefs: List<Preference>)
    fun setChangedPrefs(prefs: String)
    fun getChangedPrefs(): String
    fun setEditor(editor: SharedPreferences.Editor?)
}
