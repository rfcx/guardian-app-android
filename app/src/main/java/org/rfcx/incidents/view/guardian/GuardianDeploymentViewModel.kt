package org.rfcx.incidents.view.guardian

import android.net.wifi.ScanResult
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.guardian.socket.CloseSocketParams
import org.rfcx.incidents.domain.guardian.socket.CloseSocketUseCase
import org.rfcx.incidents.domain.guardian.socket.GetSocketMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.InitSocketUseCase
import org.rfcx.incidents.domain.guardian.socket.SendMessageParams
import org.rfcx.incidents.domain.guardian.socket.SendSocketMessageUseCase
import org.rfcx.incidents.domain.guardian.wifi.ConnectHotspotParams
import org.rfcx.incidents.domain.guardian.wifi.ConnectHotspotUseCase
import org.rfcx.incidents.domain.guardian.wifi.DisconnectHotspotUseCase
import org.rfcx.incidents.service.wifi.socket.BaseSocketManager

class GuardianDeploymentViewModel(
    private val connectHotspotUseCase: ConnectHotspotUseCase,
    private val disconnectHotspotUseCase: DisconnectHotspotUseCase,
    private val getSocketMessageUseCase: GetSocketMessageUseCase,
    private val initSocketUseCase: InitSocketUseCase,
    private val sendSocketMessageUseCase: SendSocketMessageUseCase,
    private val closeSocketUseCase: CloseSocketUseCase
) : ViewModel() {

    private val _connectionState: MutableStateFlow<Result<Boolean>?> = MutableStateFlow(null)
    val connectionState = _connectionState.asStateFlow()

    private val _socketMessageState: MutableStateFlow<Result<List<String>>?> = MutableStateFlow(null)
    val socketMessageState = _socketMessageState.asStateFlow()

    private val _initSocketState: MutableStateFlow<Result<Boolean>?> = MutableStateFlow(null)
    val initSocketState = _initSocketState.asStateFlow()

    // These coroutines are running definitely so better keep them in job for cancellation
    private var connectJob: Job? = null
    private var readChannelJob: Job? = null
    private var heartBeatJob: Job? = null

    suspend fun connectWifi(selectedHotspot: ScanResult?) {
        _connectionState.tryEmit(null)
        _socketMessageState.tryEmit(null)
        _initSocketState.tryEmit(null)
        connectJob?.cancel()
        connectJob = viewModelScope.launch(Dispatchers.IO) {
            connectHotspotUseCase.launch(ConnectHotspotParams(selectedHotspot)).collectLatest { result ->
                when (result) {
                    is Result.Error -> _connectionState.tryEmit(Result.Error(result.throwable))
                    Result.Loading -> _connectionState.tryEmit(Result.Loading)
                    is Result.Success -> {
                        Log.d("GuardianApp", "connect")
                        _connectionState.tryEmit(Result.Success(result.data))
                    }
                }
            }
        }
    }

    fun disconnectWifi() {
        viewModelScope.launch {
            // disconnectHotspotUseCase.launch()
            heartBeatJob?.cancel()
            readChannelJob?.cancel()
            connectJob?.cancel()
        }
    }

    fun initSocket() {
        viewModelScope.launch(Dispatchers.IO) {
            initSocketUseCase.launch().collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        Log.d("GuardianApp", "init failed")
                        _initSocketState.tryEmit(result)
                    }
                    Result.Loading -> {
                        Log.d("GuardianApp", "init loading")
                        _initSocketState.tryEmit(Result.Loading)
                    }
                    is Result.Success -> {
                        Log.d("GuardianApp", "init success")
                        _initSocketState.tryEmit(result)
                    }
                }
            }
        }
    }

    fun sendHeartbeatSignalPeriodic() {
        heartBeatJob?.cancel()
        heartBeatJob = null
        heartBeatJob = viewModelScope.launch(Dispatchers.IO) {
            Log.d("GuardianApp", "send heart beat")
            while (true) {
                Log.d("GuardianApp", "send heart beat2")
                sendSocketMessageUseCase.launch(SendMessageParams(BaseSocketManager.Type.ALL, "{command:\"connection\"}"))
                // To re-reference new socket
                readSocket()
                delay(10000)
            }
        }
    }

    private suspend fun readSocket() {
        readChannelJob?.cancel()
        readChannelJob = viewModelScope.launch(Dispatchers.IO) {
            getSocketMessageUseCase.launch()
                .catch {
                    _socketMessageState.tryEmit(Result.Error(it))
                }.collectLatest { result ->
                    when (result) {
                        is Result.Error -> _socketMessageState.tryEmit(result)
                        Result.Loading -> _socketMessageState.tryEmit(Result.Loading)
                        is Result.Success -> _socketMessageState.tryEmit(result)
                    }
                }
        }
    }

    fun onDestroy() {
        viewModelScope.launch(Dispatchers.IO) {
            closeSocketUseCase.launch(CloseSocketParams(BaseSocketManager.Type.ALL))
        }
    }
}
