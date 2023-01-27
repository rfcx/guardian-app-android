package org.rfcx.incidents.view.profile.guardian

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.software.SoftwareResponse
import org.rfcx.incidents.domain.guardian.software.GetSoftwareLocalUseCase
import org.rfcx.incidents.domain.guardian.software.GetSoftwareRemoteUseCase
import org.rfcx.incidents.entity.guardian.FileStatus
import org.rfcx.incidents.entity.guardian.GuardianFile
import org.rfcx.incidents.entity.guardian.GuardianFileItem

class SoftwareDownloadViewModel(
    private val getSoftwareRemoteUseCase: GetSoftwareRemoteUseCase,
    private val getSoftwareLocalUseCase: GetSoftwareLocalUseCase) : ViewModel() {

    private val _softwareItemState: MutableStateFlow<Result<List<GuardianFileItem>>> = MutableStateFlow(Result.Loading)
    val softwareItemState = _softwareItemState.asStateFlow()

    fun getSoftwareItem() {
        viewModelScope.launch(Dispatchers.IO) {
            getSoftwareRemoteUseCase.launch().combine(getSoftwareLocalUseCase.launch()) { f1, f2 ->
                Log.d("Comp", "$f1 $f2")
                if (f1 is Result.Success && f2 is Result.Success) {
                    _softwareItemState.tryEmit(Result.Success(getFileItemFromRemoteAndLocal(f1.data, f2.data)))
                }
                if (f1 is Result.Loading || f2 is Result.Loading) {
                    _softwareItemState.tryEmit(Result.Loading)
                }
                if (f1 is Result.Error){
                    _softwareItemState.tryEmit(f1)
                }
            }.collect()
        }
    }

    private fun getFileItemFromRemoteAndLocal(remote: List<SoftwareResponse>, local: List<GuardianFile>): List<GuardianFileItem> {
        val fileStatus = arrayListOf<GuardianFileItem>()
        remote.forEach { res ->
            val downloadedClassifier = local.findLast { it.role == res.role }
            if (downloadedClassifier != null) {
                val isUpToDate = res.version.toInt() == downloadedClassifier.version.toInt()
                if (isUpToDate) {
                    fileStatus.add(GuardianFileItem(res, FileStatus.UP_TO_DATE))
                } else {
                    fileStatus.add(GuardianFileItem(res, FileStatus.NEED_UPDATE))
                }
            } else {
                fileStatus.add(GuardianFileItem(res, FileStatus.NOT_DOWNLOADED))
            }
        }
        return fileStatus
    }
}
