package org.rfcx.incidents.view.guardian.checklist.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.guardian.socket.GetAdminMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.util.socket.PingUtils.getSimDetected
import org.rfcx.incidents.util.socket.PingUtils.getSimNetwork
import org.rfcx.incidents.util.socket.PingUtils.getSpeedTest
import org.rfcx.incidents.util.socket.PingUtils.getSwarmId

class NetworkTestViewModel(
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase,
    private val getAdminMessageUseCase: GetAdminMessageUseCase
) : ViewModel() {

    private val _simModuleState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val simModuleState = _simModuleState.asStateFlow()

    private val _satModuleState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val satModuleState = _satModuleState.asStateFlow()

    private val _simSignalState: MutableStateFlow<Int?> = MutableStateFlow(null)
    val simSignalState = _simSignalState.asStateFlow()

    private val _internetConnectionState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val internetConnectionState = _internetConnectionState.asStateFlow()

    init {
        getSimModule()
        getSatModule()
    }

    fun getSimModule() {
        viewModelScope.launch {
            getAdminMessageUseCase.launch().catch {

            }.collectLatest { result ->
                result?.getSimDetected()?.let {
                    _simModuleState.tryEmit(it)
                }
                result?.getSimNetwork()?.let {
                    _simSignalState.tryEmit(it)
                }
                result?.getSpeedTest()?.let {
                    _internetConnectionState.tryEmit(it.hasConnection)
                }
            }
        }
    }

    fun getSatModule() {
        viewModelScope.launch {
            getAdminMessageUseCase.launch().catch {

            }.collectLatest { result ->
                result?.getSwarmId()?.let {
                    _satModuleState.tryEmit(it.isNotEmpty())
                }
            }
        }
    }
}
