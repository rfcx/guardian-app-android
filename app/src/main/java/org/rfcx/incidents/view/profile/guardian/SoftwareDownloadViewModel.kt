package org.rfcx.incidents.view.profile.guardian

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.last
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

class SoftwareDownloadViewModel(
    private val getSoftwareRemoteUseCase: GetSoftwareRemoteUseCase,
    private val getSoftwareLocalUseCase: GetSoftwareLocalUseCase,
    private val downloadFileUseCase: DownloadFileUseCase,
    private val deleteFileUseCase: DeleteFileUseCase
) : ViewModel() {

    private val _softwareItemState: MutableStateFlow<Result<List<GuardianFileItem>>> = MutableStateFlow(Result.Loading)
    val softwareItemState = _softwareItemState.asStateFlow()

    private val _downloadSoftwareState: MutableSharedFlow<Result<List<GuardianFileItem>>> = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val downloadSoftwareState = _downloadSoftwareState.asSharedFlow()

    private val _deleteSoftwareState: MutableSharedFlow<Result<List<GuardianFileItem>>> = MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val deleteSoftwareState = _deleteSoftwareState.asSharedFlow()

    private var remoteData: List<SoftwareResponse> = emptyList()
    private var localData: List<GuardianFile> = emptyList()

    fun getSoftwareItem() {
        // Dispatcher.Main to work with local realm with created in Main thread
        viewModelScope.launch(Dispatchers.Main) {
            getSoftwareRemoteUseCase.launch().combine(getSoftwareLocalUseCase.launch()) { f1, f2 ->
                if (f1 is Result.Success && f2 is Result.Success) {
                    remoteData = f1.data
                    localData = f2.data
                    _softwareItemState.tryEmit(Result.Success(getFileItemFromRemoteAndLocal(f1.data, f2.data)))
                }
                if (f1 is Result.Loading || f2 is Result.Loading) {
                    _softwareItemState.tryEmit(Result.Loading)
                }
                if (f1 is Result.Error) {
                    _softwareItemState.tryEmit(f1)
                }
            }.collect()
        }
    }

    fun download(targetFile: GuardianFile) {
        // Dispatcher.Main to work with local realm with created in Main thread
        viewModelScope.launch(Dispatchers.Main) {
            downloadFileUseCase.launch(DownloadFileParams(targetFile)).combine(getSoftwareLocalUseCase.launch()) { f1, f2 ->
                Log.d("Comp1", f1.toString())
                Log.d("Comp2", f2.toString())
                if (f1 is Result.Success && f2 is Result.Success) {
                    localData = f2.data
                    _downloadSoftwareState.tryEmit(Result.Success(getFileItemFromRemoteAndLocal(remoteData, localData)))
                }
                if (f1 is Result.Loading || f2 is Result.Loading) {
                    _downloadSoftwareState.tryEmit(Result.Loading)
                }
                if (f1 is Result.Error) {
                    _downloadSoftwareState.tryEmit(f1)
                }
            }.collect()
        }
    }

    fun delete(targetFile: GuardianFile) {
        // Dispatcher.Main to work with local realm with created in Main thread
        viewModelScope.launch(Dispatchers.Main) {
            deleteFileUseCase.launch(DeleteFileParams(targetFile)).combine(getSoftwareLocalUseCase.launch()) { f1, f2 ->
                if (f1 is Result.Success && f2 is Result.Success) {
                    localData = f2.data
                    _deleteSoftwareState.tryEmit(Result.Success(getFileItemFromRemoteAndLocal(remoteData, f2.data)))
                }
                if (f1 is Result.Loading || f2 is Result.Loading) {
                    _deleteSoftwareState.tryEmit(Result.Loading)
                }
                if (f1 is Result.Error) {
                    _deleteSoftwareState.tryEmit(f1)
                }
            }.collect()
        }
    }

    private fun getFileItemFromRemoteAndLocal(remote: List<SoftwareResponse>, local: List<GuardianFile>): List<GuardianFileItem> {
        val fileStatus = arrayListOf<GuardianFileItem>()
        getResultToGuardianFile(remote).forEach { res ->
            val downloadedClassifier = local.findLast { it.name == res.name }
            val status = GuardianFileUtils.compareIfNeedToUpdate(res.version, downloadedClassifier?.version)
            fileStatus.add(GuardianFileItem(res, downloadedClassifier, status))
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
