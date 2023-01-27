package org.rfcx.incidents.view.profile.guardian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.data.remote.guardian.software.SoftwareResponse
import org.rfcx.incidents.domain.guardian.software.GetSoftwareUseCase

class SoftwareDownloadViewModel(private val getSoftwareUseCase: GetSoftwareUseCase) : ViewModel() {

    private val _softwareItemState: MutableStateFlow<Result<List<SoftwareResponse>>> = MutableStateFlow(Result.Loading)
    val softwareItemState = _softwareItemState.asStateFlow()

    fun getSoftwareFromRemote() {
        viewModelScope.launch(Dispatchers.IO) {
            getSoftwareUseCase.launch().collect { result ->
                _softwareItemState.tryEmit(result)
            }
        }
    }
}
