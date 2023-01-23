package org.rfcx.incidents.view.guardian.connect

import android.net.wifi.ScanResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.guardian.wifi.GetNearbyHotspotUseCase
import org.rfcx.incidents.data.remote.common.Result

class GuardianConnectViewModel(private val getNearbyHotspotUseCase: GetNearbyHotspotUseCase) : ViewModel() {

    val hotspotsState: Flow<Result<List<ScanResult>>> = getNearbyHotspotUseCase.resultFlow
    fun nearbyHotspots() {
        viewModelScope.launch {
            getNearbyHotspotUseCase.launch()
        }
    }
}
