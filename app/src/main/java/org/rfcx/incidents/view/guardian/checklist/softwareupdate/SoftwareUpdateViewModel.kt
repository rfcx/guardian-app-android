package org.rfcx.incidents.view.guardian.checklist.softwareupdate

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.domain.guardian.software.GetGuardianFileLocalParams
import org.rfcx.incidents.domain.guardian.software.GetGuardianFileLocalUseCase
import org.rfcx.incidents.entity.guardian.GuardianFile
import org.rfcx.incidents.entity.guardian.GuardianFileType
import org.rfcx.incidents.entity.guardian.GuardianFileUpdateItem
import org.rfcx.incidents.util.guardianfile.GuardianFileUtils
import org.rfcx.incidents.util.socket.PingUtils.getSoftware

class SoftwareUpdateViewModel(
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase,
    private val getGuardianFileLocalUseCase: GetGuardianFileLocalUseCase,
) : ViewModel() {

    private val _guardianSoftwareState: MutableSharedFlow<List<GuardianFileUpdateItem>> = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val guardianSoftwareState = _guardianSoftwareState.asSharedFlow()

    private var downloadedGuardianFile = emptyList<GuardianFile>()
    private var installedGuardianFile = mapOf<String, String>()

    fun getGuardianSoftware() {
        viewModelScope.launch {
            getGuardianFileLocalUseCase.launch(GetGuardianFileLocalParams(GuardianFileType.SOFTWARE)).combine(getGuardianMessageUseCase.launch()) { f1, f2 ->
                if (f2 != null) {
                    val software = f2.getSoftware()
                    if (software != null) {
                        downloadedGuardianFile = f1
                        installedGuardianFile = software
                        _guardianSoftwareState.tryEmit(getGuardianFileUpdateItem(downloadedGuardianFile, installedGuardianFile))
                    }
                }
            }.catch {
            }.collectLatest {  }
        }
    }

    private fun getGuardianFileUpdateItem(downloaded: List<GuardianFile>, installed: Map<String, String>): List<GuardianFileUpdateItem> {
        val list = arrayListOf<GuardianFileUpdateItem>()
        downloaded.forEach {
            val header = GuardianFileUpdateItem.GuardianFileUpdateHeader(it.name)
            val child = GuardianFileUpdateItem.GuardianFileUpdateVersion(it.name, it, installed[it.name], GuardianFileUtils.compareIfNeedToUpdate(installed[it.name], it.version), null)
            list.add(header)
            list.add(child)
        }
        return list
    }
}
