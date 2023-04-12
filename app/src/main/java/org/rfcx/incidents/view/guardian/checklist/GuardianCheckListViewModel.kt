package org.rfcx.incidents.view.guardian.checklist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.local.common.Constants
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.util.socket.PingUtils.getGuid
import org.rfcx.incidents.util.socket.PingUtils.isRegistered
import org.rfcx.incidents.view.guardian.GuardianScreen

class GuardianCheckListViewModel(
    private val context: Context,
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase
) : ViewModel() {

    private val _checklistItemState: MutableStateFlow<List<CheckListItem>> = MutableStateFlow(emptyList())
    val checklistItemState = _checklistItemState.asStateFlow()

    private val _registrationState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val registrationState = _registrationState.asStateFlow()

    init {
        getGuardianRegistration()
    }

    private fun getGuardianRegistration() {
        viewModelScope.launch {
            getGuardianMessageUseCase.launch().catch {

            }.collectLatest { result ->
                result?.isRegistered()?.let {
                    _registrationState.tryEmit(it)
                }
            }
        }
    }

    fun getAllCheckList(passed: List<GuardianScreen>? = listOf()) {
        val checkList = arrayListOf<CheckListItem>()
        var number = 0

        checkList.add(CheckListItem.Header("Assembly"))
        Constants.GUARDIAN_ASSEMBLY_CHECKLIST.forEach { name ->
            checkList.add(CheckListItem.CheckItem(number, name, isRequired = false, isPassed = passed?.find { it.value == number } != null))
            number++
        }

        checkList.add(CheckListItem.Header("Setup"))
        Constants.GUARDIAN_SETUP_CHECKLIST.forEach { name ->
            checkList.add(CheckListItem.CheckItem(number, name, isRequired = true, isPassed = passed?.find { it.value == number } != null))
            number++
        }

        checkList.add(CheckListItem.Header("Optional"))
        Constants.GUARDIAN_OPTIONAL_CHECKLIST.forEach { name ->
            checkList.add(CheckListItem.CheckItem(number, name, isRequired = false, isPassed = passed?.find { it.value == number } != null))
            number++
        }

        _checklistItemState.tryEmit(checkList)
    }
}
