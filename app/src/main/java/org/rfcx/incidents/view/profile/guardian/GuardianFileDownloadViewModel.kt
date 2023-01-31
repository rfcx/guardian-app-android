package org.rfcx.incidents.view.profile.guardian

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.software.ClassifierResponse
import org.rfcx.incidents.data.remote.guardian.software.GuardianFileResponse
import org.rfcx.incidents.data.remote.guardian.software.SoftwareResponse
import org.rfcx.incidents.domain.guardian.software.DeleteFileParams
import org.rfcx.incidents.domain.guardian.software.DeleteFileUseCase
import org.rfcx.incidents.domain.guardian.software.DownloadFileParams
import org.rfcx.incidents.domain.guardian.software.DownloadFileUseCase
import org.rfcx.incidents.domain.guardian.software.GetGuardianFileParams
import org.rfcx.incidents.domain.guardian.software.GetGuardianFileRemoteUseCase
import org.rfcx.incidents.domain.guardian.software.GetSoftwareLocalUseCase
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
    private val getSoftwareLocalUseCase: GetSoftwareLocalUseCase,
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
                _guardianFileItemState.tryEmit(Result.Error(Throwable("There is no internet connection")))
                listenToLocalGuardianFile()
                return@launch
            }
            getGuardianFileRemoteUseCase.launch(GetGuardianFileParams(type)).collect { result ->
                when (result) {
                    is Result.Success -> {
                        remoteData = result.data
                        listenToLocalGuardianFile()
                    }
                    Result.Loading -> _guardianFileItemState.tryEmit(Result.Loading)
                    is Result.Error -> {
                        if (result.throwable is UnknownHostException) {
                            _guardianFileItemState.tryEmit(Result.Error(Throwable("There is no internet connection")))
                        }
                    }
                }
            }
        }
    }

    fun listenToLocalGuardianFile() {
        // Dispatcher.Main to work with local realm with created in Main thread
        viewModelScope.launch(Dispatchers.Main) {
            getSoftwareLocalUseCase.launch().collect { result ->
                localData = result
                Log.d("Comp", result.toString())
                _guardianFileItemState.tryEmit(Result.Success(getFileItemFromRemoteAndLocal(remoteData, localData)))
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
            val status = GuardianFileUtils.compareIfNeedToUpdate(res.version, downloadedFile?.version)
            fileStatus.add(GuardianFileItem(res, downloadedFile, status))
        }
        return fileStatus
    }

    private fun getResultToGuardianFile(result: List<GuardianFileResponse>): List<GuardianFile> {
        return result.map {
            var meta = ""
            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
            if (it is SoftwareResponse) {
                meta = gson.toJson(it, SoftwareResponse::class.java)
            }
            if (it is ClassifierResponse) {
                meta = gson.toJson(it, ClassifierResponse::class.java)
            }

            GuardianFile(
                name = it.name, version = it.version, type = GuardianFileType.SOFTWARE.value, url = it.url, meta = meta
            )
        }
    }
}
