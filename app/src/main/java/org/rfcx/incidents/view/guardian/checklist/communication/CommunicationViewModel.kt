package org.rfcx.incidents.view.guardian.checklist.communication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.domain.GetProjectOffTimesParams
import org.rfcx.incidents.domain.GetProjectOffTimesUseCase
import org.rfcx.incidents.domain.guardian.socket.GetAdminMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.InstructionParams
import org.rfcx.incidents.domain.guardian.socket.SendInstructionCommandUseCase
import org.rfcx.incidents.entity.guardian.TimeRange
import org.rfcx.incidents.entity.guardian.socket.InstructionCommand
import org.rfcx.incidents.entity.guardian.socket.InstructionType
import org.rfcx.incidents.util.socket.GuardianPlan
import org.rfcx.incidents.util.socket.PingUtils.getGPSDetection
import org.rfcx.incidents.util.socket.PingUtils.getGuardianLocalTime
import org.rfcx.incidents.util.socket.PingUtils.getGuardianPlan
import org.rfcx.incidents.util.socket.PingUtils.getGuardianTimezone
import org.rfcx.incidents.util.socket.PingUtils.getPhoneNumber
import org.rfcx.incidents.util.socket.PingUtils.getPrefsSha1
import org.rfcx.incidents.util.socket.PingUtils.getSatTimeOff
import org.rfcx.incidents.util.socket.PingUtils.getSimDetected
import org.rfcx.incidents.util.socket.PingUtils.getSwarmId
import org.rfcx.incidents.util.socket.PrefsUtils
import org.rfcx.incidents.util.toDateTimeString
import java.util.Date

class CommunicationViewModel(
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase,
    private val getAdminMessageUseCase: GetAdminMessageUseCase,
    private val getProjectOffTimesUseCase: GetProjectOffTimesUseCase,
    private val sendInstructionCommandUseCase: SendInstructionCommandUseCase,
    private val preferences: Preferences
) : ViewModel() {

    private val _simModuleState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val simModuleState = _simModuleState.asStateFlow()

    private val _simNumberState: MutableStateFlow<String> = MutableStateFlow("")
    val simNumberState = _simNumberState.asStateFlow()

    private val _satModuleState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val satModuleState = _satModuleState.asStateFlow()

    private val _satIdState: MutableStateFlow<String> = MutableStateFlow("")
    val satIdState = _satIdState.asStateFlow()

    private val _satGPSState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val satGPSState = _satGPSState.asStateFlow()

    private val _guardianLocalTimeState: MutableStateFlow<String> = MutableStateFlow("")
    val guardianLocalTimeState = _guardianLocalTimeState.asStateFlow()

    private val _guardianLocalTimeStatusState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val guardianLocalTimeStatusState = _guardianLocalTimeStatusState.asStateFlow()

    private val _guardianLocalTimezoneState: MutableStateFlow<String> = MutableStateFlow("")
    val guardianLocalTimezoneState = _guardianLocalTimezoneState.asStateFlow()

    private val _guardianPlanCellState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val guardianPlanCellState = _guardianPlanCellState.asStateFlow()

    private val _guardianPlanSMSState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val guardianPlanSMSState = _guardianPlanSMSState.asStateFlow()

    private val _guardianPlanSatState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val guardianPlanSatState = _guardianPlanSatState.asStateFlow()

    private val _guardianPlanOfflineState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val guardianPlanOfflineState = _guardianPlanOfflineState.asStateFlow()

    private val _guardianSatTimeOffState: MutableStateFlow<String> = MutableStateFlow("")
    val guardianSatTimeOffState = _guardianSatTimeOffState.asStateFlow()

    private val _guardianSatOffTimeEmptyTextState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val guardianSatOffTimeEmptyTextState = _guardianSatOffTimeEmptyTextState.asStateFlow()

    private val _checkSha1State = MutableSharedFlow<Boolean>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val checkSha1State = _checkSha1State.asSharedFlow()

    private var currentGuardianOffTimes: String = ""
    private var currentProjectOffTimes: String = ""
    private var currentGuardianPlan = GuardianPlan.CELL_ONLY
    private var needCheckSha1 = false
    private var isFirstTime = true
    private var currentGuardianSha1 = ""
    private var isManual = true

    init {
        getPrefSha1()
        getSimModule()
        getSatModule()
        getGuardianTime()
        getGuardianPlan()
        getOffTimes()
    }

    private fun getPrefSha1() {
        viewModelScope.launch {
            getGuardianMessageUseCase.launch().catch {
            }.collectLatest { result ->
                result?.getPrefsSha1()?.let {
                    if (needCheckSha1) {
                        if (currentGuardianSha1 != it) {
                            _checkSha1State.tryEmit(true)
                            needCheckSha1 = false
                        }
                    }
                    currentGuardianSha1 = it
                }
            }
        }
    }

    private fun getSimModule() {
        viewModelScope.launch {
            getAdminMessageUseCase.launch().catch {
            }.collectLatest { result ->
                result?.getSimDetected()?.let {
                    _simModuleState.tryEmit(it)
                }
                result?.getPhoneNumber()?.let {
                    _simNumberState.tryEmit(it)
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
                    _satIdState.tryEmit(it)
                }
                result?.getGPSDetection()?.let {
                    _satGPSState.tryEmit(it)
                }
            }
        }
    }

    private fun getGuardianTime() {
        viewModelScope.launch {
            getGuardianMessageUseCase.launch().catch {
            }.collectLatest { result ->
                var localTime = 0L
                result?.getGuardianLocalTime()?.let {
                    localTime = it
                    if ((System.currentTimeMillis() - localTime) > (1000 * 60 * 60 * 24)) {
                        _guardianLocalTimeStatusState.tryEmit(false)
                    } else {
                        _guardianLocalTimeStatusState.tryEmit(true)
                    }
                }
                result?.getGuardianTimezone()?.let {
                    _guardianLocalTimezoneState.tryEmit(it)
                    if (localTime != 0L) {
                        _guardianLocalTimeState.tryEmit(Date(localTime).toDateTimeString(it))
                    }
                }
            }
        }
    }

    private fun getGuardianPlan() {
        viewModelScope.launch {
            getGuardianMessageUseCase.launch().catch {
            }.collectLatest { result ->
                result?.getGuardianPlan()?.let {
                    currentGuardianPlan = it
                    when (it) {
                        GuardianPlan.CELL_ONLY -> {
                            _guardianPlanCellState.tryEmit(true)
                            _guardianPlanSMSState.tryEmit(false)
                            _guardianPlanSatState.tryEmit(false)
                            _guardianPlanOfflineState.tryEmit(false)
                        }
                        GuardianPlan.CELL_SMS -> {
                            _guardianPlanCellState.tryEmit(false)
                            _guardianPlanSMSState.tryEmit(true)
                            _guardianPlanSatState.tryEmit(false)
                            _guardianPlanOfflineState.tryEmit(false)
                        }
                        GuardianPlan.SAT_ONLY -> {
                            _guardianPlanCellState.tryEmit(false)
                            _guardianPlanSMSState.tryEmit(false)
                            _guardianPlanSatState.tryEmit(true)
                            _guardianPlanOfflineState.tryEmit(false)
                        }
                        GuardianPlan.OFFLINE_MODE -> {
                            _guardianPlanCellState.tryEmit(false)
                            _guardianPlanSMSState.tryEmit(false)
                            _guardianPlanSatState.tryEmit(false)
                            _guardianPlanOfflineState.tryEmit(true)
                        }
                    }
                }
            }
        }
    }

    private fun getOffTimes() {
        viewModelScope.launch {
            getGuardianMessageUseCase.launch().catch {
            }.collectLatest { result ->
                result?.getSatTimeOff()?.let {
                    if (isFirstTime) {
                        currentGuardianOffTimes = it
                        _guardianSatTimeOffState.tryEmit(currentGuardianOffTimes)
                        isFirstTime = false
                    }
                }
            }
        }
        viewModelScope.launch {
            getProjectOffTimesUseCase.launch(GetProjectOffTimesParams(preferences.getString(Preferences.SELECTED_PROJECT, ""))).catch {
            }.collectLatest { result ->
                currentProjectOffTimes = result
                determineEmptyProjectOffTimes()
            }
        }
    }

    private fun determineEmptyProjectOffTimes() {
        if (currentProjectOffTimes.isEmpty() && !isManual) {
            _guardianSatOffTimeEmptyTextState.tryEmit(true)
        } else {
            _guardianSatOffTimeEmptyTextState.tryEmit(false)
        }
    }

    fun onManualClicked() {
        isManual = true
        _guardianSatTimeOffState.tryEmit(currentGuardianOffTimes)
        determineEmptyProjectOffTimes()
    }

    fun onAutoClicked() {
        isManual = false
        _guardianSatTimeOffState.tryEmit(currentProjectOffTimes)
        determineEmptyProjectOffTimes()
    }

    fun onNextClicked(plan: GuardianPlan, offTimes: List<TimeRange>? = null, isManual: Boolean = true) {
        when (plan) {
            GuardianPlan.CELL_ONLY -> {
                if (currentGuardianPlan != GuardianPlan.CELL_ONLY) needCheckSha1 = true
                viewModelScope.launch(Dispatchers.IO) {
                    sendInstructionCommandUseCase.launch(
                        InstructionParams(
                            InstructionType.SET,
                            InstructionCommand.PREFS,
                            PrefsUtils.getCellOnlyPrefs().toString()
                        )
                    )
                }
            }
            GuardianPlan.CELL_SMS -> {
                if (currentGuardianPlan != GuardianPlan.CELL_SMS) needCheckSha1 = true
                viewModelScope.launch(Dispatchers.IO) {
                    sendInstructionCommandUseCase.launch(
                        InstructionParams(
                            InstructionType.SET,
                            InstructionCommand.PREFS,
                            PrefsUtils.getCellSMSPrefs().toString()
                        )
                    )
                }
            }
            GuardianPlan.SAT_ONLY -> {
                if (currentGuardianPlan != GuardianPlan.SAT_ONLY) needCheckSha1 = true
                if (isManual) {
                    if (currentGuardianOffTimes != offTimes?.joinToString(",") { it.toStringFormat() }) needCheckSha1 = true
                    viewModelScope.launch(Dispatchers.IO) {
                        sendInstructionCommandUseCase.launch(
                            InstructionParams(
                                InstructionType.SET,
                                InstructionCommand.PREFS,
                                PrefsUtils.getSatOnlyPrefs(offTimes?.joinToString(",") { it.toStringFormat() } ?: "").toString()
                            )
                        )
                    }
                } else {
                    if (currentProjectOffTimes.isNotEmpty()) {
                        if (currentGuardianOffTimes != currentProjectOffTimes) needCheckSha1 = true
                        viewModelScope.launch(Dispatchers.IO) {
                            sendInstructionCommandUseCase.launch(
                                InstructionParams(
                                    InstructionType.SET,
                                    InstructionCommand.PREFS,
                                    PrefsUtils.getSatOnlyPrefs(currentProjectOffTimes).toString()
                                )
                            )
                        }
                    } else {
                        viewModelScope.launch(Dispatchers.IO) {
                            sendInstructionCommandUseCase.launch(
                                InstructionParams(
                                    InstructionType.SET,
                                    InstructionCommand.PREFS,
                                    PrefsUtils.getSatOnlyPrefs().toString()
                                )
                            )
                        }
                    }
                }
            }
            GuardianPlan.OFFLINE_MODE -> {
                if (currentGuardianPlan != GuardianPlan.OFFLINE_MODE) needCheckSha1 = true
                viewModelScope.launch(Dispatchers.IO) {
                    sendInstructionCommandUseCase.launch(
                        InstructionParams(
                            InstructionType.SET,
                            InstructionCommand.PREFS,
                            PrefsUtils.getOfflineModePrefs().toString()
                        )
                    )
                }
            }
        }
        if (!needCheckSha1) {
            _checkSha1State.tryEmit(true)
        }
    }
}
