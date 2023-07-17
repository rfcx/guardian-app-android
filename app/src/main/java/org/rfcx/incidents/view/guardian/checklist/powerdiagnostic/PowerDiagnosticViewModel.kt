package org.rfcx.incidents.view.guardian.checklist.powerdiagnostic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.guardian.socket.GetAdminMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.entity.guardian.socket.I2CAccessibility
import org.rfcx.incidents.util.socket.PingUtils.getI2cAccessibility
import org.rfcx.incidents.util.socket.PingUtils.getInternalBattery
import org.rfcx.incidents.util.socket.PingUtils.getSentinelPower

class PowerDiagnosticViewModel(
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase,
    private val getAdminMessageUseCase: GetAdminMessageUseCase
) : ViewModel() {

    private val _i2cAccessibilityState: MutableStateFlow<I2CAccessibility> = MutableStateFlow(I2CAccessibility())
    val i2cAccessibilityState = _i2cAccessibilityState.asStateFlow()

    private val _voltageState: MutableStateFlow<String?> = MutableStateFlow(null)
    val voltageState = _voltageState.asStateFlow()

    private val _currentState: MutableStateFlow<String?> = MutableStateFlow(null)
    val currentState = _currentState.asStateFlow()

    private val _powerState: MutableStateFlow<String?> = MutableStateFlow(null)
    val powerState = _powerState.asStateFlow()

    private val _mainBttState: MutableStateFlow<String?> = MutableStateFlow(null)
    val mainBttState = _mainBttState.asStateFlow()

    private val _internalBttState: MutableStateFlow<String?> = MutableStateFlow(null)
    val internalBttState = _internalBttState.asStateFlow()

    private val _bttVoltage: MutableStateFlow<String?> = MutableStateFlow(null)
    val bttVoltage = _bttVoltage.asStateFlow()

    private val _sysVoltage: MutableStateFlow<String?> = MutableStateFlow(null)
    val sysVoltage = _sysVoltage.asStateFlow()

    private val _powerChartState: MutableStateFlow<Entry?> = MutableStateFlow(null)
    val powerChartState = _powerChartState.asStateFlow()

    init {
        getI2CAvailability()
        getSentinelPower()
        getInternalBattery()
    }

    private fun getI2CAvailability() {
        viewModelScope.launch {
            getAdminMessageUseCase.launch().catch {
            }.collectLatest { result ->
                if (result != null) {
                    result.getI2cAccessibility()?.let { accessibility ->
                        _i2cAccessibilityState.tryEmit(accessibility)
                    }
                }
            }
        }
    }

    private fun getSentinelPower() {
        viewModelScope.launch {
            var entryIndex = 0
            getAdminMessageUseCase.launch().catch {
            }.collectLatest { result ->
                result?.getSentinelPower()?.let { sentinelPower ->
                    _voltageState.tryEmit("${sentinelPower.input.voltage}mV")
                    _currentState.tryEmit("${sentinelPower.input.current}mA")
                    _powerState.tryEmit("${sentinelPower.input.power}mW")
                    _mainBttState.tryEmit("${sentinelPower.battery.percentage}%")
                    _bttVoltage.tryEmit("${sentinelPower.battery.voltage}mV")
                    _sysVoltage.tryEmit("${sentinelPower.system.voltage}mV")

                    _powerChartState.tryEmit(Entry((entryIndex).toFloat(), sentinelPower.input.power.toFloat()))
                    entryIndex++
                }
            }
        }
    }

    private fun getInternalBattery() {
        viewModelScope.launch {
            getGuardianMessageUseCase.launch().catch {
            }.collectLatest { result ->
                result?.getInternalBattery()?.let { btt ->
                    _internalBttState.tryEmit("$btt%")
                }
            }
        }
    }
}
