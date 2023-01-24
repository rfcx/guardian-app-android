package org.rfcx.incidents.view.guardian.connect

import android.net.wifi.ScanResult
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.guardian.socket.GetSocketMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.InitSocketUseCase
import org.rfcx.incidents.domain.guardian.wifi.ConnectHotspotParams
import org.rfcx.incidents.domain.guardian.wifi.ConnectHotspotUseCase
import org.rfcx.incidents.domain.guardian.wifi.GetNearbyHotspotUseCase

class GuardianConnectViewModel(
    private val getNearbyHotspotUseCase: GetNearbyHotspotUseCase,
    private val connectHotspotUseCase: ConnectHotspotUseCase,
    private val getSocketMessageUseCase: GetSocketMessageUseCase,
    private val initSocketUseCase: InitSocketUseCase
) : ViewModel() {

    private val _hotspotsState: MutableStateFlow<Result<List<ScanResult>>> = MutableStateFlow(Result.Loading)
    val hotspotsState = _hotspotsState.asStateFlow()

    private val _connectionState: MutableStateFlow<Result<Boolean>> = MutableStateFlow(Result.Loading)
    val connectionState: Flow<Result<Boolean>> = _connectionState.asStateFlow()

    private val _socketMessageState: MutableStateFlow<Result<String>> = MutableStateFlow(Result.Loading)
    val socketMessageState: Flow<Result<String>> = _socketMessageState.asStateFlow()

    private val _initSocketState: MutableStateFlow<Result<Boolean>> = MutableStateFlow(Result.Loading)
    val initSocketState: Flow<Result<Boolean>> = _initSocketState.asStateFlow()

    private var selectedHotspot: ScanResult? = null

    fun nearbyHotspots() {
        viewModelScope.launch {
            getNearbyHotspotUseCase.launch().collectLatest { result ->
                when (result) {
                    is Result.Error -> _hotspotsState.value = Result.Error(result.throwable)
                    Result.Loading -> _hotspotsState.value = Result.Loading
                    is Result.Success -> _hotspotsState.value = Result.Success(result.data)
                }
            }
        }
    }

    fun connect() {
        viewModelScope.launch {
            connectHotspotUseCase.launch(ConnectHotspotParams(selectedHotspot)).collectLatest { result ->
                when (result) {
                    is Result.Error -> _connectionState.value = Result.Error(result.throwable)
                    Result.Loading -> _connectionState.value = Result.Loading
                    is Result.Success -> _connectionState.value = Result.Success(result.data)
                }
            }
        }
    }

    fun setSelectedHotspot(hotspot: ScanResult) {
        selectedHotspot = hotspot
    }

    fun initSocket() {
        viewModelScope.launch(Dispatchers.IO) {
            initSocketUseCase.launch().collectLatest { result ->
                when (result) {
                    is Result.Error -> _initSocketState.value = Result.Error(result.throwable)
                    Result.Loading -> _initSocketState.value = Result.Loading
                    is Result.Success -> _initSocketState.value = Result.Success(result.data)
                }
            }
        }
    }

    fun readSocket() {
        viewModelScope.launch(Dispatchers.IO) {
            getSocketMessageUseCase.launch().collectLatest { result ->
                when (result) {
                    is Result.Error -> _socketMessageState.value = Result.Error(result.throwable)
                    Result.Loading -> _socketMessageState.value = Result.Loading
                    is Result.Success -> _socketMessageState.value = Result.Success(result.data)
                }
            }
        }
    }
}
