package org.rfcx.incidents.view.guardian.checklist.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.guardian.socket.GetAdminMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.InstructionParams
import org.rfcx.incidents.domain.guardian.socket.SendInstructionCommandUseCase
import org.rfcx.incidents.entity.guardian.socket.InstructionCommand
import org.rfcx.incidents.entity.guardian.socket.InstructionType
import org.rfcx.incidents.entity.guardian.socket.SpeedTest
import org.rfcx.incidents.util.socket.PingUtils.getSimDetected
import org.rfcx.incidents.util.socket.PingUtils.getSimNetwork
import org.rfcx.incidents.util.socket.PingUtils.getSpeedTest
import org.rfcx.incidents.util.socket.PingUtils.getSwarmId
import org.rfcx.incidents.util.socket.PingUtils.getSwarmNetwork

class NetworkTestViewModel(
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase,
    private val getAdminMessageUseCase: GetAdminMessageUseCase,
    private val sendInstructionCommandUseCase: SendInstructionCommandUseCase
) : ViewModel() {

    private val _simModuleState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val simModuleState = _simModuleState.asStateFlow()

    private val _satModuleState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val satModuleState = _satModuleState.asStateFlow()

    private val _simSignalState: MutableStateFlow<Int?> = MutableStateFlow(null)
    val simSignalState = _simSignalState.asStateFlow()

    private val _simStrength1State: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val simStrength1State = _simStrength1State.asStateFlow()

    private val _simStrength2State: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val simStrength2State = _simStrength2State.asStateFlow()

    private val _simStrength3State: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val simStrength3State = _simStrength3State.asStateFlow()

    private val _simStrength4State: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val simStrength4State = _simStrength4State.asStateFlow()

    private val _internetConnectionState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val internetConnectionState = _internetConnectionState.asStateFlow()

    private val _satSignalState: MutableStateFlow<Int?> = MutableStateFlow(null)
    val satSignalState = _satSignalState.asStateFlow()

    private val _satErrorState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val satErrorState = _satErrorState.asStateFlow()

    private val _satBadState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val satBadState = _satBadState.asStateFlow()

    private val _satOKState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val satOKState = _satOKState.asStateFlow()

    private val _satGoodState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val satGoodState = _satGoodState.asStateFlow()

    private val _satPerfectState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val satPerfectState = _satPerfectState.asStateFlow()

    private val _downloadTestState: MutableStateFlow<String> = MutableStateFlow("read to test")
    val downloadTestState = _downloadTestState.asStateFlow()

    private val _uploadTestState: MutableStateFlow<String> = MutableStateFlow("read to test")
    val uploadTestState = _uploadTestState.asStateFlow()

    private val _testButtonState: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val testButtonState = _testButtonState.asStateFlow()

    init {
        getSimModule()
        getSatModule()
    }

    private fun getSimModule() {
        viewModelScope.launch {
            getAdminMessageUseCase.launch().catch {
            }.collectLatest { result ->
                result?.getSimDetected()?.let {
                    _simModuleState.tryEmit(it)
                }
                result?.getSimNetwork()?.let {
                    _simSignalState.tryEmit(it)
                    setSimStrengthState(it)
                }
                result?.getSpeedTest()?.let {
                    _internetConnectionState.tryEmit(it.hasConnection)
                    setDownloadUpdateState(it)
                }
            }
        }
    }

    private fun getSatModule() {
        viewModelScope.launch {
            getAdminMessageUseCase.launch().catch {
            }.collectLatest { result ->
                result?.getSwarmId()?.let {
                    _satModuleState.tryEmit(it.isNotEmpty())
                }
            }
        }
        viewModelScope.launch {
            getGuardianMessageUseCase.launch().catch {
            }.collectLatest { result ->
                result?.getSwarmNetwork()?.let {
                    _satSignalState.tryEmit(it)
                    setSatStrengthState(it)
                }
            }
        }
    }

    fun sendSpeedTestCommand() {
        viewModelScope.launch(Dispatchers.IO) {
            // show waiting ui
            _testButtonState.tryEmit(false)
            _downloadTestState.tryEmit("in testing")
            _uploadTestState.tryEmit("in testing")
            sendInstructionCommandUseCase.launch(InstructionParams(InstructionType.CTRL, InstructionCommand.SPEED_TEST))
        }
    }

    private fun setSimStrengthState(signal: Int) {
        when {
            signal < -130 -> {
                _simStrength1State.tryEmit(false)
                _simStrength2State.tryEmit(false)
                _simStrength3State.tryEmit(false)
                _simStrength4State.tryEmit(false)
            }
            signal < -110 -> {
                _simStrength1State.tryEmit(true)
                _simStrength2State.tryEmit(false)
                _simStrength3State.tryEmit(false)
                _simStrength4State.tryEmit(false)
            }
            signal < -90 -> {
                _simStrength1State.tryEmit(true)
                _simStrength2State.tryEmit(true)
                _simStrength3State.tryEmit(false)
                _simStrength4State.tryEmit(false)
            }
            signal < -70 -> {
                _simStrength1State.tryEmit(true)
                _simStrength2State.tryEmit(true)
                _simStrength3State.tryEmit(true)
                _simStrength4State.tryEmit(false)
            }
            else -> {
                _simStrength1State.tryEmit(true)
                _simStrength2State.tryEmit(true)
                _simStrength3State.tryEmit(true)
                _simStrength4State.tryEmit(true)
            }
        }
    }

    private fun setSatStrengthState(signal: Int) {
        when {
            signal <= -110 -> {
                _satErrorState.tryEmit(true)
                _satBadState.tryEmit(false)
                _satOKState.tryEmit(false)
                _satGoodState.tryEmit(false)
                _satPerfectState.tryEmit(false)
            }
            signal <= -104 -> {
                _satErrorState.tryEmit(false)
                _satBadState.tryEmit(false)
                _satOKState.tryEmit(false)
                _satGoodState.tryEmit(false)
                _satPerfectState.tryEmit(true)
            }
            signal < -100 -> {
                _satErrorState.tryEmit(false)
                _satBadState.tryEmit(false)
                _satOKState.tryEmit(false)
                _satGoodState.tryEmit(true)
                _satPerfectState.tryEmit(false)
            }
            signal < -97 -> {
                _satErrorState.tryEmit(false)
                _satBadState.tryEmit(false)
                _satOKState.tryEmit(true)
                _satGoodState.tryEmit(false)
                _satPerfectState.tryEmit(false)
            }
            signal < -93 -> {
                _satErrorState.tryEmit(false)
                _satBadState.tryEmit(true)
                _satOKState.tryEmit(false)
                _satGoodState.tryEmit(false)
                _satPerfectState.tryEmit(false)
            }
        }
    }

    private fun setDownloadUpdateState(speedTest: SpeedTest) {
        if (speedTest.isTesting) {
            _testButtonState.tryEmit(false)
            _downloadTestState.tryEmit("in testing")
            _uploadTestState.tryEmit("in testing")
        } else if (speedTest.isFailed) {
            _downloadTestState.tryEmit("testing failed")
            _uploadTestState.tryEmit("testing failed")
        } else if (speedTest.downloadSpeed == -1.0 && speedTest.uploadSpeed == -1.0) {
            _downloadTestState.tryEmit(String.format("ready to test", speedTest.downloadSpeed))
            _uploadTestState.tryEmit(String.format("ready to test", speedTest.uploadSpeed))
        } else {
            _testButtonState.tryEmit(true)
            _downloadTestState.tryEmit(String.format("%.2f kb/s download", speedTest.downloadSpeed))
            _uploadTestState.tryEmit(String.format("%.2f kb/s upload", speedTest.uploadSpeed))
        }
    }
}
