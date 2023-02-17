package org.rfcx.incidents.view.guardian.checklist.classifierupload

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
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.SendFileSocketParams
import org.rfcx.incidents.domain.guardian.socket.SendFileSocketUseCase
import org.rfcx.incidents.domain.guardian.guardianfile.GetGuardianFileLocalParams
import org.rfcx.incidents.domain.guardian.guardianfile.GetGuardianFileLocalUseCase
import org.rfcx.incidents.entity.guardian.GuardianFile
import org.rfcx.incidents.entity.guardian.GuardianFileType
import org.rfcx.incidents.entity.guardian.GuardianFileUpdateItem
import org.rfcx.incidents.entity.guardian.UpdateStatus
import org.rfcx.incidents.util.guardianfile.GuardianFileUtils
import org.rfcx.incidents.util.socket.PingUtils.getClassifiers

class ClassifierUploadViewModel(
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase,
    private val getGuardianFileLocalUseCase: GetGuardianFileLocalUseCase,
    private val sendFileSocketUseCase: SendFileSocketUseCase
) : ViewModel() {

    private val _guardianClassifierState: MutableSharedFlow<List<GuardianFileUpdateItem>> = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val guardianClassifierState = _guardianClassifierState.asSharedFlow()

    private var downloadedGuardianFile = emptyList<GuardianFile>()
    private var installedGuardianFile = mapOf<String, String>()

    private var isUploading = false
    private var targetFile: GuardianFile? = null

    fun getGuardianClassifier() {
        viewModelScope.launch {
            getGuardianFileLocalUseCase.launch(GetGuardianFileLocalParams(GuardianFileType.CLASSIFIER)).combine(getGuardianMessageUseCase.launch()) { f1, f2 ->
                if (f2 != null) {
                    val classifier = f2.getClassifiers()
                    if (classifier != null) {
                        downloadedGuardianFile = f1
                        installedGuardianFile = classifier
                        if (installedGuardianFile[targetFile?.name] == targetFile?.version) {
                            isUploading = false
                            targetFile = null
                        }
                        _guardianClassifierState.tryEmit(getGuardianFileUpdateItem(downloadedGuardianFile, installedGuardianFile))
                    }
                }
            }.catch {

            }.collect()
        }
    }

    private fun getGuardianFileUpdateItem(downloaded: List<GuardianFile>, installed: Map<String, String>): List<GuardianFileUpdateItem> {
        val list = arrayListOf<GuardianFileUpdateItem>()
        downloaded.forEach {
            val header = GuardianFileUpdateItem.GuardianFileUpdateHeader(it.name)
            val status = if (isUploading && it.name == targetFile?.name) {
                UpdateStatus.LOADING
            } else if (isUploading) {
                if (GuardianFileUtils.compareIfNeedToUpdate(installed[it.name], it.version) == UpdateStatus.UP_TO_DATE) {
                    UpdateStatus.UP_TO_DATE
                } else {
                    UpdateStatus.WAITING
                }
            } else {
                GuardianFileUtils.compareIfNeedToUpdate(installed[it.name], it.version)
            }
            val child = GuardianFileUpdateItem.GuardianFileUpdateVersion(it.name, it, installed[it.name], status, null)
            list.add(header)
            list.add(child)
        }
        return list
    }

    fun updateOrInstallGuardianFile(file: GuardianFile) {
        isUploading = true
        targetFile = file
        _guardianClassifierState.tryEmit(getGuardianFileUpdateItem(downloadedGuardianFile, installedGuardianFile))
        viewModelScope.launch {
            sendFileSocketUseCase.launch(SendFileSocketParams(file)).collectLatest { result ->
                when(result) {
                    is Result.Error -> {
                        isUploading = false
                        targetFile = null
                    }
                    Result.Loading -> {

                    }
                    is Result.Success -> {
                    }
                }
            }
        }
    }
}
