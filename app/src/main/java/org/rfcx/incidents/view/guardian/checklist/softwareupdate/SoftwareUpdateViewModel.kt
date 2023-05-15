package org.rfcx.incidents.view.guardian.checklist.softwareupdate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.guardian.guardianfile.GetGuardianFileLocalParams
import org.rfcx.incidents.domain.guardian.guardianfile.GetGuardianFileLocalUseCase
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.SendFileSocketParams
import org.rfcx.incidents.domain.guardian.socket.SendFileSocketUseCase
import org.rfcx.incidents.entity.guardian.file.GuardianFile
import org.rfcx.incidents.entity.guardian.file.GuardianFileType
import org.rfcx.incidents.entity.guardian.SoftwareUpdateItem
import org.rfcx.incidents.entity.guardian.UpdateStatus
import org.rfcx.incidents.entity.guardian.socket.OperationType
import org.rfcx.incidents.util.guardianfile.GuardianFileUtils
import org.rfcx.incidents.util.socket.PingUtils.getSoftware

class SoftwareUpdateViewModel(
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase,
    private val getGuardianFileLocalUseCase: GetGuardianFileLocalUseCase,
    private val sendFileSocketUseCase: SendFileSocketUseCase
) : ViewModel() {

    private val _guardianSoftwareState: MutableSharedFlow<List<SoftwareUpdateItem>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val guardianSoftwareState = _guardianSoftwareState.asSharedFlow()

    private var downloadedSoftware = emptyList<GuardianFile>()
    private var installedSoftware = mapOf<String, String>()

    private val _isOperating = MutableStateFlow(false)
    val isOperating = _isOperating.asStateFlow()
    private var operatingType: OperationType? = null

    private var targetProgress = 0
    private var targetFile: GuardianFile? = null

    fun getGuardianSoftware() {
        viewModelScope.launch {
            getGuardianFileLocalUseCase.launch(GetGuardianFileLocalParams(GuardianFileType.SOFTWARE)).combine(getGuardianMessageUseCase.launch()) { f1, f2 ->
                downloadedSoftware = f1
                if (f2 != null) {
                    val software = f2.getSoftware()
                    if (software != null) {
                        installedSoftware = software
                    }
                    handleLoadingAndSetting()
                    _guardianSoftwareState.tryEmit(getSoftwareUpdateItem(downloadedSoftware, installedSoftware, targetProgress))
                }
            }.catch {

            }.collect()
        }
    }

    private fun handleLoadingAndSetting() {
        if (operatingType == OperationType.INSTALL && installedSoftware[targetFile?.name] == targetFile?.version) {
            _isOperating.tryEmit(false)
            targetFile = null
            operatingType = null
            targetProgress = 0
        }
    }

    private fun getSoftwareUpdateItem(downloaded: List<GuardianFile>, installed: Map<String, String>, progress: Int): List<SoftwareUpdateItem> {
        val list = arrayListOf<SoftwareUpdateItem>()
        downloaded.forEach {
            val header = SoftwareUpdateItem.SoftwareUpdateHeader(it.name)
            val child = SoftwareUpdateItem.SoftwareUpdateVersion(
                it.name,
                it,
                installed[it.name],
                GuardianFileUtils.compareIfNeedToUpdate(installed[it.name], it.version),
                true,
                progress
            )
            if (_isOperating.value && it.name == targetFile?.name) {
                child.status = UpdateStatus.LOADING
            }
            if (_isOperating.value && it.name != targetFile?.name) {
                child.isEnabled = false
            }
            list.add(header)
            list.add(child)
        }
        return list
    }

    fun updateOrInstallSoftware(file: GuardianFile) {
        _isOperating.tryEmit(true)
        targetFile = file
        operatingType = OperationType.INSTALL
        _guardianSoftwareState.tryEmit(getSoftwareUpdateItem(downloadedSoftware, installedSoftware, targetProgress))
        viewModelScope.launch {
            sendFileSocketUseCase.launch(SendFileSocketParams(file)).catch {
                _isOperating.tryEmit(false)
                operatingType = null
                targetFile = null
                targetProgress = 0
            }.collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        _isOperating.tryEmit(false)
                        operatingType = null
                        targetFile = null
                    }
                    Result.Loading -> {}
                    is Result.Success -> {
                        if (!result.data.isSuccess) {
                            targetProgress = result.data.progress
                        }
                        _guardianSoftwareState.tryEmit(getSoftwareUpdateItem(downloadedSoftware, installedSoftware, targetProgress))
                    }
                }
            }
        }
    }
}
