package org.rfcx.incidents.view.guardian.checklist.classifierupload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.guardian.guardianfile.GetGuardianFileLocalParams
import org.rfcx.incidents.domain.guardian.guardianfile.GetGuardianFileLocalUseCase
import org.rfcx.incidents.domain.guardian.socket.GetGuardianMessageUseCase
import org.rfcx.incidents.domain.guardian.socket.InstructionParams
import org.rfcx.incidents.domain.guardian.socket.SendFileSocketParams
import org.rfcx.incidents.domain.guardian.socket.SendFileSocketUseCase
import org.rfcx.incidents.domain.guardian.socket.SendInstructionCommandUseCase
import org.rfcx.incidents.entity.guardian.ClassifierUploadItem
import org.rfcx.incidents.entity.guardian.GuardianFile
import org.rfcx.incidents.entity.guardian.GuardianFileType
import org.rfcx.incidents.entity.guardian.UpdateStatus
import org.rfcx.incidents.entity.guardian.socket.ClassifierSet
import org.rfcx.incidents.entity.guardian.socket.InstructionCommand
import org.rfcx.incidents.entity.guardian.socket.InstructionType
import org.rfcx.incidents.util.guardianfile.GuardianFileUtils
import org.rfcx.incidents.util.socket.PingUtils.getActiveClassifiers
import org.rfcx.incidents.util.socket.PingUtils.getClassifiers

class ClassifierUploadViewModel(
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase,
    private val getGuardianFileLocalUseCase: GetGuardianFileLocalUseCase,
    private val sendFileSocketUseCase: SendFileSocketUseCase,
    private val sendInstructionCommandUseCase: SendInstructionCommandUseCase
) : ViewModel() {

    private val _guardianClassifierState: MutableSharedFlow<List<ClassifierUploadItem>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val guardianClassifierState = _guardianClassifierState.asSharedFlow()

    private var downloadedGuardianFile = emptyList<GuardianFile>()
    private var installedGuardianFile = mapOf<String, String>()
    private var activeClassifiers = mapOf<String, String>()

    private var isUploading = false
    private var isSetting = false
    private var targetActivate: Boolean? = null
    private var targetFile: GuardianFile? = null

    fun getGuardianClassifier() {
        viewModelScope.launch {
            getGuardianFileLocalUseCase.launch(GetGuardianFileLocalParams(GuardianFileType.CLASSIFIER)).combine(getGuardianMessageUseCase.launch()) { f1, f2 ->
                downloadedGuardianFile = f1
                if (f2 != null) {
                    val classifier = f2.getClassifiers()
                    val activeClassifier = f2.getActiveClassifiers()
                    installedGuardianFile = classifier
                    activeClassifiers = activeClassifier

                    if (installedGuardianFile[targetFile?.name] == targetFile?.version) {
                        isUploading = false
                    }
                    if (targetActivate == true) {
                        if (activeClassifiers[targetFile?.name] != null) {
                            isSetting = false
                        }
                    }
                    if (targetActivate == false) {
                        if (activeClassifiers[targetFile?.name] == null) {
                            isSetting = false
                        }
                    }
                    _guardianClassifierState.tryEmit(getClassifierUpdateItem(downloadedGuardianFile, installedGuardianFile, activeClassifiers))
                }
            }.catch {

            }.collect()
        }
    }

    private fun getClassifierUpdateItem(
        downloaded: List<GuardianFile>,
        installed: Map<String, String>,
        actives: Map<String, String>
    ): List<ClassifierUploadItem> {
        val list = arrayListOf<ClassifierUploadItem>()
        downloaded.forEach {
            val header = ClassifierUploadItem.ClassifierUploadHeader(it.name)
            val child = ClassifierUploadItem.ClassifierUploadVersion(
                it.name, it, installed[it.name], GuardianFileUtils.compareIfNeedToUpdate(installed[it.name], it.version),
                isEnabled = true,
                isActive = false,
                progress = null
            )
            if ((isUploading || isSetting) && it.name == targetFile?.name) {
                child.status = UpdateStatus.LOADING
            }
            if ((isUploading || isSetting) && it.name != targetFile?.name) {
                child.isEnabled = false
            }

            actives.keys.forEach { key ->
                if (installed[key] != null) {
                    child.isActive = true
                }
            }
            list.add(header)
            list.add(child)
        }
        return list
    }

    fun updateOrInstallGuardianFile(file: GuardianFile) {
        isUploading = true
        targetFile = file
        _guardianClassifierState.tryEmit(getClassifierUpdateItem(downloadedGuardianFile, installedGuardianFile, activeClassifiers))
        viewModelScope.launch {
            sendFileSocketUseCase.launch(SendFileSocketParams(file)).collectLatest { result ->
                when (result) {
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

    fun activateClassifier(file: GuardianFile) {
        isSetting = true
        targetFile = file
        targetActivate = true
        _guardianClassifierState.tryEmit(getClassifierUpdateItem(downloadedGuardianFile, installedGuardianFile, activeClassifiers))

        val gson = Gson()
        val meta = gson.fromJson(file.meta, JsonObject::class.java)
        viewModelScope.launch(Dispatchers.IO) {
            sendInstructionCommandUseCase.launch(
                InstructionParams(
                    InstructionType.SET,
                    InstructionCommand.CLASSIFIER,
                    gson.toJson(ClassifierSet("activate", meta.get("id").asString))
                )
            )
        }
    }

    fun deActivateClassifier(file: GuardianFile) {
        isSetting = true
        targetFile = file
        targetActivate = false
        _guardianClassifierState.tryEmit(getClassifierUpdateItem(downloadedGuardianFile, installedGuardianFile, activeClassifiers))

        val gson = Gson()
        val meta = gson.fromJson(file.meta, JsonObject::class.java)
        viewModelScope.launch(Dispatchers.IO) {
            sendInstructionCommandUseCase.launch(
                InstructionParams(
                    InstructionType.SET,
                    InstructionCommand.CLASSIFIER,
                    gson.toJson(ClassifierSet("deactivate", meta.get("id").asString))
                )
            )
        }
    }
}
