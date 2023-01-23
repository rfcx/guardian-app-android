package org.rfcx.incidents.view.guardian.connect

import android.net.wifi.ScanResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.guardian.wifi.GetNearbyHotspotUseCase
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.guardian.wifi.ConnectHotspotParams
import org.rfcx.incidents.domain.guardian.wifi.ConnectHotspotUseCase

class GuardianConnectViewModel(
    private val getNearbyHotspotUseCase: GetNearbyHotspotUseCase,
    private val connectHotspotUseCase: ConnectHotspotUseCase,
    ) : ViewModel() {

    val hotspotsState: Flow<Result<List<ScanResult>>> = getNearbyHotspotUseCase.resultFlow
    val connectionState: Flow<Result<Boolean>> = connectHotspotUseCase.resultFlow
    private var selectedHotspot: ScanResult? = null

    fun nearbyHotspots() {
        viewModelScope.launch {
            getNearbyHotspotUseCase.launch()
        }
    }

    fun connect() {
        viewModelScope.launch {
            connectHotspotUseCase.launch(ConnectHotspotParams(selectedHotspot))
        }
    }

    fun setSelectedHotspot(hotspot: ScanResult) {
        selectedHotspot = hotspot
    }
}
