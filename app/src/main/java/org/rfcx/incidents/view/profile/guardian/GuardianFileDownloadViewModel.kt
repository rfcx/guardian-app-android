package org.rfcx.incidents.view.profile.guardian

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.remote.common.NoConnectionException
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.software.ClassifierResponse
import org.rfcx.incidents.data.remote.guardian.software.GuardianFileResponse
import org.rfcx.incidents.data.remote.guardian.software.SoftwareResponse
import org.rfcx.incidents.domain.guardian.guardianfile.DeleteFileParams
import org.rfcx.incidents.domain.guardian.guardianfile.DeleteFileUseCase
import org.rfcx.incidents.domain.guardian.guardianfile.DownloadFileParams
import org.rfcx.incidents.domain.guardian.guardianfile.DownloadFileUseCase
import org.rfcx.incidents.domain.guardian.guardianfile.GetGuardianFileLocalParams
import org.rfcx.incidents.domain.guardian.guardianfile.GetGuardianFileLocalUseCase
import org.rfcx.incidents.domain.guardian.guardianfile.GetGuardianFileParams
import org.rfcx.incidents.domain.guardian.guardianfile.GetGuardianFileRemoteUseCase
import org.rfcx.incidents.entity.guardian.FileStatus
import org.rfcx.incidents.entity.guardian.GuardianFile
import org.rfcx.incidents.entity.guardian.GuardianFileItem
import org.rfcx.incidents.entity.guardian.GuardianFileType
import org.rfcx.incidents.util.guardianfile.GuardianFileUtils
import org.rfcx.incidents.util.isNetworkAvailable
import java.net.UnknownHostException

class GuardianFileDownloadViewModel(
    private val context: Context,
    private val getGuardianFileRemoteUseCase: GetGuardianFileRemoteUseCase,
    private val getGuardianFileLocalUseCase: GetGuardianFileLocalUseCase,
    private val downloadFileUseCase: DownloadFileUseCase,
    private val deleteFileUseCase: DeleteFileUseCase
) : ViewModel() {

    private val _guardianFileItemState: MutableStateFlow<Result<List<GuardianFileItem>>> = MutableStateFlow(Result.Loading)
    val guardianFileItemState = _guardianFileItemState.asStateFlow()

    private val _downloadGuardianFileState: MutableSharedFlow<Result<Boolean>> = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val downloadGuardianFileState = _downloadGuardianFileState.asSharedFlow()

    private val _deleteGuardianFileState: MutableSharedFlow<Result<Boolean>> = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val deleteGuardianFileState = _deleteGuardianFileState.asSharedFlow()

    private var remoteData: List<GuardianFileResponse> = emptyList()
    private var localData: List<GuardianFile> = emptyList()

    fun getSoftwareItem() {
        getGuardianFileItem(GuardianFileType.SOFTWARE)
    }

    fun getClassifierItem() {
        getGuardianFileItem(GuardianFileType.CLASSIFIER)
    }

    private fun getGuardianFileItem(type: GuardianFileType) {
        // Dispatcher.Main to work with local realm with created in Main thread
        viewModelScope.launch(Dispatchers.Main) {
            if (!context.isNetworkAvailable()) {
                _guardianFileItemState.tryEmit(Result.Error(NoConnectionException()))
                listenToLocal(type)
                return@launch
            }
            getGuardianFileRemoteUseCase.launch(GetGuardianFileParams(type)).collect { result ->
                when (result) {
                    is Result.Success -> {
                        remoteData = result.data
                        listenToLocal(type)
                    }
                    Result.Loading -> _guardianFileItemState.tryEmit(Result.Loading)
                    is Result.Error -> {
                        if (result.throwable is UnknownHostException) {
                            _guardianFileItemState.tryEmit(Result.Error(Throwable(NoConnectionException())))
                        } else {
                            _guardianFileItemState.tryEmit(result)
                        }
                        listenToLocal(type)
                    }
                }
            }
        }
    }

    private fun listenToLocal(type: GuardianFileType) {
        when(type) {
            GuardianFileType.SOFTWARE -> listenToLocalSoftware()
            GuardianFileType.CLASSIFIER -> listenToLocalClassifier()
        }
    }

    private fun listenToLocalSoftware() {
        listenToLocalGuardianFile(GuardianFileType.SOFTWARE)
    }

    private fun listenToLocalClassifier() {
        listenToLocalGuardianFile(GuardianFileType.CLASSIFIER)
    }

    private fun listenToLocalGuardianFile(type: GuardianFileType) {
        // Dispatcher.Main to work with local realm with created in Main thread
        viewModelScope.launch(Dispatchers.Main) {
            getGuardianFileLocalUseCase.launch(GetGuardianFileLocalParams(type)).map { result ->
                localData = result
                getFileItemFromRemoteAndLocal(remoteData, localData)
            }.collect { result ->
                _guardianFileItemState.tryEmit(Result.Success(result))
            }
        }
    }


    fun download(targetFile: GuardianFile) {
        // Dispatcher.Main to work with local realm with created in Main thread
        viewModelScope.launch(Dispatchers.Main) {
            downloadFileUseCase.launch(DownloadFileParams(targetFile)).collect { result ->
                when (result) {
                    is Result.Error -> _downloadGuardianFileState.tryEmit(result)
                    Result.Loading -> _downloadGuardianFileState.tryEmit(Result.Loading)
                    is Result.Success -> _downloadGuardianFileState.tryEmit(result)
                }
            }
        }
    }

    fun delete(targetFile: GuardianFile) {
        // Dispatcher.Main to work with local realm with created in Main thread
        viewModelScope.launch(Dispatchers.Main) {
            deleteFileUseCase.launch(DeleteFileParams(targetFile)).collect { result ->
                when (result) {
                    is Result.Error -> _deleteGuardianFileState.tryEmit(result)
                    Result.Loading -> _deleteGuardianFileState.tryEmit(Result.Loading)
                    is Result.Success -> _deleteGuardianFileState.tryEmit(result)
                }
            }
        }
    }

    private fun getFileItemFromRemoteAndLocal(remote: List<GuardianFileResponse>, local: List<GuardianFile>): List<GuardianFileItem> {
        val fileStatus = arrayListOf<GuardianFileItem>()
        if (remote.isEmpty()) return local.map { GuardianFileItem(null, it, FileStatus.NO_INTERNET) }

        getResultToGuardianFile(remote).forEach { res ->
            val downloadedFile = local.find { it.name == res.name }
            val status = GuardianFileUtils.compareIfNeedToDownload(res.version, downloadedFile?.version)
            fileStatus.add(GuardianFileItem(res, downloadedFile, status))
        }
        return fileStatus
    }

    private fun getResultToGuardianFile(result: List<GuardianFileResponse>): List<GuardianFile> {
        return result.map {
            val obj = GuardianFile(
                id = it.name, name = it.name, version = it.version, url = it.url
            )
            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
            if (it is SoftwareResponse) {
                val meta = gson.toJson(it, SoftwareResponse::class.java)
                obj.meta = meta
                obj.type = GuardianFileType.SOFTWARE.value
            }
            if (it is ClassifierResponse) {
                obj.id = it.id
                val subObj = JsonObject()
                subObj.addProperty("classifier_name", it.name)
                subObj.addProperty("classifier_version", it.version)
                subObj.addProperty("sample_rate", it.sampleRate)
                subObj.addProperty("input_gain", it.inputGain)
                subObj.addProperty("window_size", it.windowSize)
                subObj.addProperty("step_size", it.stepSize)
                subObj.addProperty("classifications", it.classifications)
                subObj.addProperty("classifications_filter_threshold", it.classificationsFilterThreshold)

                val metaClassifier = JsonObject()
                metaClassifier.addProperty("asset_id", it.id)
                metaClassifier.addProperty("file_type", it.classifierType)
                metaClassifier.addProperty("checksum", it.sha1)
                metaClassifier.addProperty("meta_json_blob", gson.toJson(subObj))

                obj.meta = gson.toJson(metaClassifier)
                obj.type = GuardianFileType.CLASSIFIER.value
            }
            obj
        }
    }
}
