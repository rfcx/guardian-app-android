package org.rfcx.incidents.view.guardian.checklist.communication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.domain.GetProjectOffTimesParams
import org.rfcx.incidents.domain.GetProjectOffTimesUseCase
import org.rfcx.incidents.domain.guardian.socket.GetAdminMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.util.socket.GuardianPlan
import org.rfcx.incidents.util.socket.PingUtils.getGPSDetection
import org.rfcx.incidents.util.socket.PingUtils.getGuardianLocalTime
import org.rfcx.incidents.util.socket.PingUtils.getGuardianPlan
import org.rfcx.incidents.util.socket.PingUtils.getGuardianTimezone
import org.rfcx.incidents.util.socket.PingUtils.getPhoneNumber
import org.rfcx.incidents.util.socket.PingUtils.getSatTimeOff
import org.rfcx.incidents.util.socket.PingUtils.getSimDetected
import org.rfcx.incidents.util.socket.PingUtils.getSwarmId
import org.rfcx.incidents.util.toDateTimeString
import java.util.Date

class CommunicationViewModel(
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase,
    private val getAdminMessageUseCase: GetAdminMessageUseCase,
    private val getProjectOffTimesUseCase: GetProjectOffTimesUseCase,
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

    private var currentGuardianOffTimes: String = ""
    private var currentProjectOffTimes: String = ""

    init {
        getSimModule()
        getSatModule()
        getGuardianTime()
        getGuardianPlan()
        getOffTimes()
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
                    when(it) {
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
                    currentGuardianOffTimes = it
                }
            }
        }
        viewModelScope.launch {
            getProjectOffTimesUseCase.launch(GetProjectOffTimesParams(preferences.getString(Preferences.SELECTED_PROJECT, ""))).catch {

            }.collectLatest { result ->
                currentProjectOffTimes = result
            }
        }
    }

    fun onManualClicked() {
        _guardianSatTimeOffState.tryEmit(currentGuardianOffTimes)
    }

    fun onAutoClicked() {
        _guardianSatTimeOffState.tryEmit(currentProjectOffTimes)
    }
}
