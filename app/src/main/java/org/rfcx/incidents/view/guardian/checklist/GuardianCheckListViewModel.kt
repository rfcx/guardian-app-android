package org.rfcx.incidents.view.guardian.checklist

import android.content.Context
import android.net.wifi.ScanResult
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.rfcx.incidents.R
import org.rfcx.incidents.data.local.common.Constants
import org.rfcx.incidents.data.remote.common.Result

class GuardianCheckListViewModel(
    private val context: Context
): ViewModel() {

    private val _checklistItemState: MutableStateFlow<List<CheckListItem>> = MutableStateFlow(emptyList())
    val checklistItemState = _checklistItemState.asStateFlow()

    fun getAllCheckList() {
        val checkList = arrayListOf<CheckListItem>()
        var number = 0

        checkList.add(CheckListItem.Header("Assembly"))
        Constants.GUARDIAN_ASSEMBLY_CHECKLIST.forEach { name ->
            checkList.add(CheckListItem.CheckItem(number, name, isRequired = false))
            number++
        }

        checkList.add(CheckListItem.Header("Setup"))
        Constants.GUARDIAN_SETUP_CHECKLIST.forEach { name ->
            checkList.add(CheckListItem.CheckItem(number, name, isRequired = true))
            number++
        }

        checkList.add(CheckListItem.Header("Optional"))
        Constants.GUARDIAN_OPTIONAL_CHECKLIST.forEach { name ->
            checkList.add(CheckListItem.CheckItem(number, name, isRequired = false))
            number++
        }

        _checklistItemState.tryEmit(checkList)
    }
}
