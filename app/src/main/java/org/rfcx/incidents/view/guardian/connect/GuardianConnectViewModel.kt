package org.rfcx.incidents.view.guardian.connect

import android.net.wifi.ScanResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.guardian.wifi.ConnectHotspotParams
import org.rfcx.incidents.domain.guardian.wifi.ConnectHotspotUseCase
import org.rfcx.incidents.domain.guardian.wifi.GetNearbyHotspotUseCase

class GuardianConnectViewModel(
    private val getNearbyHotspotUseCase: GetNearbyHotspotUseCase,
    private val connectHotspotUseCase: ConnectHotspotUseCase
) : ViewModel() {

    private val _hotspotsState: MutableSharedFlow<Result<List<ScanResult>>> = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val hotspotsState = _hotspotsState.asSharedFlow()

    private val _connectionState: MutableSharedFlow<Result<Boolean>> = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val connectionState = _connectionState.asSharedFlow()

    private var selectedHotspot: ScanResult? = null

    // These coroutines are running definitely so better keep them in job for cancellation
    private var getNearbyJob: Job? = null
    private var connectJob: Job? = null

    suspend fun nearbyHotspots() {
        getNearbyJob?.cancel()
        getNearbyJob = viewModelScope.launch(Dispatchers.IO) {
            getNearbyHotspotUseCase.launch().collectLatest { result ->
                when (result) {
                    is Result.Error -> _hotspotsState.tryEmit(Result.Error(result.throwable))
                    Result.Loading -> _hotspotsState.tryEmit(Result.Loading)
                    is Result.Success -> _hotspotsState.tryEmit(Result.Success(result.data))
                }
            }
        }
    }

    suspend fun connect() {
        connectJob?.cancel()
        connectJob = viewModelScope.launch(Dispatchers.IO) {
            connectHotspotUseCase.launch(ConnectHotspotParams(selectedHotspot)).collectLatest { result ->
                when (result) {
                    is Result.Error -> _connectionState.tryEmit(Result.Error(result.throwable))
                    Result.Loading -> _connectionState.tryEmit(Result.Loading)
                    is Result.Success -> _connectionState.tryEmit(Result.Success(result.data))
                }
            }
        }
    }

    fun setSelectedHotspot(hotspot: ScanResult) {
        selectedHotspot = hotspot
    }
}
