package org.rfcx.incidents.view.guardian.checklist.powerdiagnostic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.guardian.socket.GetAdminMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.entity.guardian.socket.I2CAccessibility
import org.rfcx.incidents.util.socket.PingUtils.getI2cAccessibility

class PowerDiagnosticViewModel(
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase,
    private val getAdminMessageUseCase: GetAdminMessageUseCase
): ViewModel() {

    private val _i2cAccessibilityState: MutableStateFlow<I2CAccessibility> = MutableStateFlow(I2CAccessibility())
    val i2cAccessibilityState = _i2cAccessibilityState.asStateFlow()

    init {
        getI2CAvailability()
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
}
