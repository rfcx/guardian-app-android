package org.rfcx.incidents.view.guardian.checklist.classifierupload

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
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

    private val _errorClassifierState = MutableStateFlow(false)
    val errorClassifierState = _errorClassifierState.asStateFlow()

    private var downloadedGuardianFile = emptyList<GuardianFile>()
    private var installedGuardianFile = mapOf<String, String>()
    private var activeClassifiers = mapOf<String, String>()

    private var isUploading = false
    private var isSetting = false
    private var targetActivate: Boolean? = null
    private var targetSetFile: GuardianFile? = null
    private var targetUploadFile: GuardianFile? = null

    private var classifierTimer: CountDownTimer? = null

    fun getGuardianClassifier() {
        viewModelScope.launch {
            getGuardianFileLocalUseCase.launch(GetGuardianFileLocalParams(GuardianFileType.CLASSIFIER)).combine(getGuardianMessageUseCase.launch()) { f1, f2 ->
                downloadedGuardianFile = f1
                if (f2 != null) {
                    val classifier = f2.getClassifiers()
                    val activeClassifier = f2.getActiveClassifiers()
                    installedGuardianFile = classifier
                    activeClassifiers = activeClassifier

                    if (installedGuardianFile[targetUploadFile?.name] == targetUploadFile?.version) {
                        isUploading = false
                        targetUploadFile = null
                        stopTimer()
                    }
                    if (targetActivate == true) {
                        if (activeClassifiers[targetSetFile?.name] != null) {
                            isSetting = false
                            targetActivate = null
                            targetSetFile = null
                            stopTimer()
                        }
                    }
                    if (targetActivate == false) {
                        if (activeClassifiers[targetSetFile?.name] == null) {
                            isSetting = false
                            targetActivate = null
                            targetSetFile = null
                            stopTimer()
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
            if ((isUploading || isSetting) && it.name == targetUploadFile?.name) {
                child.status = UpdateStatus.LOADING
            }
            if ((isUploading || isSetting) && it.name != targetUploadFile?.name) {
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
        targetUploadFile = file
        _guardianClassifierState.tryEmit(getClassifierUpdateItem(downloadedGuardianFile, installedGuardianFile, activeClassifiers))
        viewModelScope.launch {
            sendFileSocketUseCase.launch(SendFileSocketParams(file)).collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        isUploading = false
                        targetUploadFile = null
                    }
                    Result.Loading -> {
                    }
                    is Result.Success -> {
                    }
                }
            }
        }
        startTimer()
    }

    fun activateClassifier(file: GuardianFile) {
        isSetting = true
        targetUploadFile = file
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
        startTimer()
    }

    fun deActivateClassifier(file: GuardianFile) {
        isSetting = true
        targetUploadFile = file
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
        startTimer()
    }

    fun restartService() {
        viewModelScope.launch(Dispatchers.IO) {
            val gson = Gson()
            val json = JsonObject()
            json.addProperty("service", "file-socket")
            sendInstructionCommandUseCase.launch(
                InstructionParams(
                    InstructionType.CTRL,
                    InstructionCommand.RESTART,
                    gson.toJson(json)
                )
            )
        }
    }

    private fun startTimer() {
        classifierTimer = object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                if (isUploading || isSetting) {
                    Log.d("Comp5", "error")
                    _errorClassifierState.tryEmit(true)
                    isUploading = false
                    targetUploadFile = null
                    isSetting = false
                    targetActivate = null
                    targetSetFile = null
                }
                stopTimer()
            }
        }
        classifierTimer?.start()
    }

    private fun stopTimer() {
        classifierTimer?.cancel()
        classifierTimer = null
    }
}
