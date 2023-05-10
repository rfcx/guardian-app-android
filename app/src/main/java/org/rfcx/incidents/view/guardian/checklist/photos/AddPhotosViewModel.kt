package org.rfcx.incidents.view.guardian.checklist.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.util.socket.GuardianPlan
import org.rfcx.incidents.util.socket.PingUtils.getGuardianPlan

class AddPhotosViewModel(
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase
) : ViewModel() {

    var guardianPlan = GuardianPlan.CELL_ONLY

    init {
        getGuardianPlan()
    }

    private fun getGuardianPlan() {
        viewModelScope.launch {
            getGuardianMessageUseCase.launch().catch {

            }.collectLatest { result ->
                result?.getGuardianPlan()?.let {
                    guardianPlan = it
                }
            }
        }
    }
}
