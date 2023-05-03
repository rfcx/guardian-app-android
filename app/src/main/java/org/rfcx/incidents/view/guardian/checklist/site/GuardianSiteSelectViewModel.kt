package org.rfcx.incidents.view.guardian.checklist.site

import android.location.Location
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.domain.GetLocalStreamsParams
import org.rfcx.incidents.domain.GetLocalStreamsUseCase
import org.rfcx.incidents.util.getListSite

class GuardianSiteSelectViewModel(
    private val getLocalStreamsUseCase: GetLocalStreamsUseCase,
    private val preferences: Preferences
) : ViewModel() {

    private val _streams: MutableStateFlow<List<SiteWithDistanceItem>> = MutableStateFlow(emptyList())
    val streams = _streams.asStateFlow()

    init {
        getStreams()
    }

    private fun getStreams() {
        viewModelScope.launch {
            getLocalStreamsUseCase.launch(GetLocalStreamsParams(preferences.getString(Preferences.SELECTED_PROJECT)!!)).catch {

            }.collectLatest { result ->
                val loc = Location(LocationManager.GPS_PROVIDER)
                loc.latitude = 0.0
                loc.longitude = 0.0
                val siteItems = getListSite(loc, result)
                _streams.tryEmit(siteItems)
            }
        }
    }
}
