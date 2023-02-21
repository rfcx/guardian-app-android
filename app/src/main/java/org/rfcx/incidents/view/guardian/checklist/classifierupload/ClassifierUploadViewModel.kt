package org.rfcx.incidents.view.guardian.checklist.classifierupload

import android.os.CountDownTimer
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.remote.common.GuardianModeNotCompatibleException
import org.rfcx.incidents.data.remote.common.NoActiveClassifierException
import org.rfcx.incidents.data.remote.common.OperationTimeoutException
import org.rfcx.incidents.data.remote.common.SoftwareNotCompatibleException
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
import org.rfcx.incidents.entity.guardian.socket.GuardianPing
import org.rfcx.incidents.entity.guardian.socket.InstructionCommand
import org.rfcx.incidents.entity.guardian.socket.InstructionType
import org.rfcx.incidents.entity.guardian.socket.OperationType
import org.rfcx.incidents.util.guardianfile.GuardianFileUtils
import org.rfcx.incidents.util.socket.PingUtils.canGuardianClassify
import org.rfcx.incidents.util.socket.PingUtils.getActiveClassifiers
import org.rfcx.incidents.util.socket.PingUtils.getClassifiers
import org.rfcx.incidents.util.socket.PingUtils.getSoftware

class ClassifierUploadViewModel(
    private val getGuardianMessageUseCase: GetGuardianMessageUseCase,
    private val getGuardianFileLocalUseCase: GetGuardianFileLocalUseCase,
    private val sendFileSocketUseCase: SendFileSocketUseCase,
    private val sendInstructionCommandUseCase: SendInstructionCommandUseCase
) : ViewModel() {

    private val _guardianClassifierState: MutableSharedFlow<List<ClassifierUploadItem>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val guardianClassifierState = _guardianClassifierState.asSharedFlow()

    private val _errorClassifierState: MutableStateFlow<Exception?> = MutableStateFlow(null)
    val errorClassifierState = _errorClassifierState.asStateFlow()

    private var downloadedClassifiers = emptyList<GuardianFile>()
    private var installedClassifiers = mapOf<String, String>()
    private var activeClassifiers = mapOf<String, String>()

    private val _isOperating = MutableStateFlow(false)
    val isOperating = _isOperating.asStateFlow()
    private var operatingType: OperationType? = null

    private var targetActivate: Boolean? = null
    private var targetSetFile: GuardianFile? = null

    private var classifierTimer: CountDownTimer? = null

    private val REQUIRED_VERSION = 10100

    fun getGuardianClassifier() {
        viewModelScope.launch {
            getGuardianFileLocalUseCase.launch(GetGuardianFileLocalParams(GuardianFileType.CLASSIFIER)).combine(getGuardianMessageUseCase.launch()) { f1, f2 ->
                downloadedClassifiers = f1
                if (f2 != null) {
                    checkClassifyRequirement(f2)
                    val classifier = f2.getClassifiers()
                    val activeClassifier = f2.getActiveClassifiers()
                    installedClassifiers = classifier
                    activeClassifiers = activeClassifier

                    handleLoadingAndSetting()
                    _guardianClassifierState.tryEmit(getClassifierUpdateItem(downloadedClassifiers, installedClassifiers, activeClassifiers))
                }
            }.catch {

            }.collect()
        }
    }

    private fun handleLoadingAndSetting() {
        if (operatingType == OperationType.INSTALL && installedClassifiers[targetSetFile?.name] == targetSetFile?.version) {
            _isOperating.tryEmit(false)
            targetSetFile = null
            operatingType = null
            stopTimer()
        }
        if (operatingType == OperationType.ACTIVATION && targetActivate == true) {
            if (activeClassifiers[targetSetFile?.name] != null) {
                _isOperating.tryEmit(false)
                targetActivate = null
                targetSetFile = null
                operatingType = null
                stopTimer()
            }
        }
        if (operatingType == OperationType.ACTIVATION && targetActivate == false) {
            if (activeClassifiers[targetSetFile?.name] == null) {
                _isOperating.tryEmit(false)
                targetActivate = null
                targetSetFile = null
                operatingType = null
                stopTimer()
            }
        }
    }

    private fun checkClassifyRequirement(guardianPing: GuardianPing) {
        if (!isSoftwareCompatible(guardianPing)) {
            _errorClassifierState.tryEmit(SoftwareNotCompatibleException())
        } else if (!isGuardianModeCompatible(guardianPing)) {
            _errorClassifierState.tryEmit(GuardianModeNotCompatibleException())
        } else if (!isThereActiveClassifier(guardianPing)) {
            _errorClassifierState.tryEmit(NoActiveClassifierException())
        } else {
            _errorClassifierState.tryEmit(null)
        }
    }

    private fun isSoftwareCompatible(guardianPing: GuardianPing): Boolean {
        val software = guardianPing.getSoftware() ?: return false

        if (!software.containsKey("guardian")) return false
        val guardian = software["guardian"] ?: return false
        if (GuardianFileUtils.calculateVersionValue(guardian) < REQUIRED_VERSION) return false

        if (!software.containsKey("classify")) return false
        val classify = software["classify"] ?: return false
        if (GuardianFileUtils.calculateVersionValue(classify) < REQUIRED_VERSION) return false

        return true
    }

    private fun isGuardianModeCompatible(guardianPing: GuardianPing): Boolean {
        return guardianPing.canGuardianClassify()
    }

    private fun isThereActiveClassifier(guardianPing: GuardianPing): Boolean {
        return guardianPing.getActiveClassifiers().isNotEmpty()
    }

    private fun getClassifierUpdateItem(
        downloaded: List<GuardianFile>, installed: Map<String, String>, actives: Map<String, String>
    ): List<ClassifierUploadItem> {
        val list = arrayListOf<ClassifierUploadItem>()
        downloaded.forEach {
            val header = ClassifierUploadItem.ClassifierUploadHeader(it.name)
            val child = ClassifierUploadItem.ClassifierUploadVersion(
                it.name, it, installed[it.name], GuardianFileUtils.compareIfNeedToUpdate(installed[it.name], it.version),
                isEnabled = true, isActive = false, progress = null
            )
            if ((isOperating.value) && it.name == targetSetFile?.name) {
                child.status = UpdateStatus.LOADING
            }
            if ((isOperating.value) && it.name != targetSetFile?.name) {
                child.isEnabled = false
            }

            if (installed[it.name] != null && actives[it.name] != null) {
                child.isActive = true
            }
            list.add(header)
            list.add(child)
        }
        return list
    }

    fun updateOrUploadClassifier(file: GuardianFile) {
        _isOperating.tryEmit(true)
        targetSetFile = file
        operatingType = OperationType.INSTALL
        _guardianClassifierState.tryEmit(getClassifierUpdateItem(downloadedClassifiers, installedClassifiers, activeClassifiers))
        viewModelScope.launch {
            sendFileSocketUseCase.launch(SendFileSocketParams(file)).catch {
                _isOperating.tryEmit(false)
                operatingType = null
                targetSetFile = null
            }.collect()
        }
        startTimer()
    }

    fun activateClassifier(file: GuardianFile) {
        _isOperating.tryEmit(true)
        targetSetFile = file
        targetActivate = true
        operatingType = OperationType.ACTIVATION
        _guardianClassifierState.tryEmit(getClassifierUpdateItem(downloadedClassifiers, installedClassifiers, activeClassifiers))

        val gson = Gson()
        viewModelScope.launch(Dispatchers.IO) {
            sendInstructionCommandUseCase.launch(
                InstructionParams(
                    InstructionType.SET,
                    InstructionCommand.CLASSIFIER, gson.toJson(ClassifierSet("activate", file.id))
                )
            )
        }
        startTimer()
    }

    fun deActivateClassifier(file: GuardianFile) {
        _isOperating.tryEmit(true)
        targetSetFile = file
        targetActivate = false
        operatingType = OperationType.ACTIVATION
        _guardianClassifierState.tryEmit(getClassifierUpdateItem(downloadedClassifiers, installedClassifiers, activeClassifiers))

        val gson = Gson()
        viewModelScope.launch(Dispatchers.IO) {
            sendInstructionCommandUseCase.launch(
                InstructionParams(
                    InstructionType.SET,
                    InstructionCommand.CLASSIFIER, gson.toJson(ClassifierSet("deactivate", file.id))
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
                if (_isOperating.value) {
                    _errorClassifierState.tryEmit(OperationTimeoutException())
                    _isOperating.tryEmit(false)
                    operatingType = null
                    targetSetFile = null
                    targetActivate = null
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
