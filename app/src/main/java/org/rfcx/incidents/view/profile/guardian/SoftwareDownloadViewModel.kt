package org.rfcx.incidents.view.profile.guardian

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.software.SoftwareResponse
import org.rfcx.incidents.domain.guardian.software.DeleteFileParams
import org.rfcx.incidents.domain.guardian.software.DeleteFileUseCase
import org.rfcx.incidents.domain.guardian.software.DownloadFileParams
import org.rfcx.incidents.domain.guardian.software.DownloadFileUseCase
import org.rfcx.incidents.domain.guardian.software.GetSoftwareLocalUseCase
import org.rfcx.incidents.domain.guardian.software.GetSoftwareRemoteUseCase
import org.rfcx.incidents.entity.guardian.FileStatus
import org.rfcx.incidents.entity.guardian.GuardianFile
import org.rfcx.incidents.entity.guardian.GuardianFileItem
import org.rfcx.incidents.entity.guardian.GuardianFileType
import org.rfcx.incidents.util.guardianfile.GuardianFileUtils
import org.rfcx.incidents.util.isNetworkAvailable
import java.net.UnknownHostException

class SoftwareDownloadViewModel(
    private val context: Context,
    private val getSoftwareRemoteUseCase: GetSoftwareRemoteUseCase,
    private val getSoftwareLocalUseCase: GetSoftwareLocalUseCase,
    private val downloadFileUseCase: DownloadFileUseCase,
    private val deleteFileUseCase: DeleteFileUseCase
) : ViewModel() {

    private val _softwareItemState: MutableStateFlow<Result<List<GuardianFileItem>>> = MutableStateFlow(Result.Loading)
    val softwareItemState = _softwareItemState.asStateFlow()

    private val _downloadSoftwareState: MutableSharedFlow<Result<Boolean>> = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val downloadSoftwareState = _downloadSoftwareState.asSharedFlow()

    private val _deleteSoftwareState: MutableSharedFlow<Result<Boolean>> = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val deleteSoftwareState = _deleteSoftwareState.asSharedFlow()

    private var remoteData: List<SoftwareResponse> = emptyList()
    private var localData: List<GuardianFile> = emptyList()

    fun getSoftwareItem() {
        // Dispatcher.Main to work with local realm with created in Main thread
        viewModelScope.launch(Dispatchers.Main) {
            if (!context.isNetworkAvailable()) {
                _softwareItemState.tryEmit(Result.Error(Throwable("There is no internet connection")))
                listenToLocalGuardianFile()
                return@launch
            }
            getSoftwareRemoteUseCase.launch().collect { result ->
                when (result) {
                    is Result.Success -> {
                        remoteData = result.data
                        listenToLocalGuardianFile()
                    }
                    Result.Loading -> _softwareItemState.tryEmit(Result.Loading)
                    is Result.Error -> {
                        if (result.throwable is UnknownHostException) {
                            _softwareItemState.tryEmit(Result.Error(Throwable("There is no internet connection")))
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
                _softwareItemState.tryEmit(Result.Success(getFileItemFromRemoteAndLocal(remoteData, localData)))
            }
        }
    }

    fun download(targetFile: GuardianFile) {
        // Dispatcher.Main to work with local realm with created in Main thread
        viewModelScope.launch(Dispatchers.Main) {
            downloadFileUseCase.launch(DownloadFileParams(targetFile)).collect { result ->
                when (result) {
                    is Result.Error -> _downloadSoftwareState.tryEmit(result)
                    Result.Loading -> _downloadSoftwareState.tryEmit(Result.Loading)
                    is Result.Success -> _downloadSoftwareState.tryEmit(result)
                }
            }
        }
    }

    fun delete(targetFile: GuardianFile) {
        // Dispatcher.Main to work with local realm with created in Main thread
        viewModelScope.launch(Dispatchers.Main) {
            deleteFileUseCase.launch(DeleteFileParams(targetFile)).collect { result ->
                when (result) {
                    is Result.Error -> _deleteSoftwareState.tryEmit(result)
                    Result.Loading -> _deleteSoftwareState.tryEmit(Result.Loading)
                    is Result.Success -> _deleteSoftwareState.tryEmit(result)
                }
            }
        }
    }

    private fun getFileItemFromRemoteAndLocal(remote: List<SoftwareResponse>, local: List<GuardianFile>): List<GuardianFileItem> {
        val fileStatus = arrayListOf<GuardianFileItem>()
        if (remote.isEmpty()) return local.map { GuardianFileItem(null, it, FileStatus.NO_INTERNET) }

        getResultToGuardianFile(remote).forEach { res ->
            val downloadedFile = local.find { it.name == res.name }
            val status = GuardianFileUtils.compareIfNeedToUpdate(res.version, downloadedFile?.version)
            fileStatus.add(GuardianFileItem(res, downloadedFile, status))
        }
        return fileStatus
    }

    private fun getResultToGuardianFile(result: List<SoftwareResponse>): List<GuardianFile> {
        return result.map {
            GuardianFile(
                name = it.role,
                version = it.version,
                type = GuardianFileType.SOFTWARE.value,
                url = it.url,
                size = it.size,
                sha1 = it.sha1
            )
        }
    }
}
