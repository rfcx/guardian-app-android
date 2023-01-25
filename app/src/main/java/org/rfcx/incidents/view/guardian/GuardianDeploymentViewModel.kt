package org.rfcx.incidents.view.guardian

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
import org.rfcx.incidents.domain.guardian.socket.GetSocketMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.InitSocketUseCase

class GuardianDeploymentViewModel(
    private val getSocketMessageUseCase: GetSocketMessageUseCase,
    private val initSocketUseCase: InitSocketUseCase
) : ViewModel() {

    private val _socketMessageState: MutableSharedFlow<Result<String>> = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val socketMessageState = _socketMessageState.asSharedFlow()

    private val _initSocketState: MutableSharedFlow<Result<Boolean>> = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val initSocketState = _initSocketState.asSharedFlow()

    // This coroutine is running definitely so better keep it in job for cancellation
    private var readChannelJob: Job? = null

    fun initSocket() {
        viewModelScope.launch(Dispatchers.IO) {
            initSocketUseCase.launch().collectLatest { result ->
                when (result) {
                    is Result.Error -> _initSocketState.tryEmit(Result.Error(result.throwable))
                    Result.Loading -> _initSocketState.tryEmit(Result.Loading)
                    is Result.Success -> _initSocketState.tryEmit(Result.Success(result.data))
                }
            }
        }
    }

    suspend fun readSocket() {
        readChannelJob?.cancel()
        readChannelJob = viewModelScope.launch(Dispatchers.IO) {
            getSocketMessageUseCase.launch().collectLatest { result ->
                when (result) {
                    is Result.Error -> _socketMessageState.tryEmit(Result.Error(result.throwable))
                    Result.Loading -> _socketMessageState.tryEmit(Result.Loading)
                    is Result.Success -> _socketMessageState.tryEmit(Result.Success(result.data))
                }
            }
        }
    }
}
