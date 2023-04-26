package org.rfcx.incidents.view.guardian

import android.net.wifi.ScanResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _connectionState: MutableSharedFlow<Result<Boolean>> = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val connectionState = _connectionState.asSharedFlow()

    private val _socketMessageState: MutableSharedFlow<Result<List<String>>> = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val socketMessageState = _socketMessageState.asSharedFlow()

    private val _initSocketState: MutableSharedFlow<Result<Boolean>> = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val initSocketState = _initSocketState.asSharedFlow()

    // These coroutines are running definitely so better keep them in job for cancellation
    private var connectJob: Job? = null
    private var readChannelJob: Job? = null
    private var heartBeatJob: Job? = null

    suspend fun connectWifi(selectedHotspot: ScanResult?) {
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

    fun disconnectWifi() {
        viewModelScope.launch {
            disconnectHotspotUseCase.launch()
        }
    }

    fun initSocket() {
        viewModelScope.launch(Dispatchers.IO) {
            initSocketUseCase.launch().collectLatest { result ->
                when (result) {
                    is Result.Error -> _initSocketState.tryEmit(result)
                    Result.Loading -> _initSocketState.tryEmit(Result.Loading)
                    is Result.Success -> _initSocketState.tryEmit(result)
                }
            }
        }
    }

    fun sendHeartbeatSignalPeriodic() {
        heartBeatJob?.cancel()
        heartBeatJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
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
