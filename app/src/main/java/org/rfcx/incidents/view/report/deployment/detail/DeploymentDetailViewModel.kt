package org.rfcx.incidents.view.report.deployment.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.GetLocalLiveStreamUseCase
import org.rfcx.incidents.domain.GetLocalStreamParams
import org.rfcx.incidents.domain.GetLocalStreamUseCase
import org.rfcx.incidents.domain.GetLocalStreamsUseCase
import org.rfcx.incidents.entity.stream.Stream

class DeploymentDetailViewModel(
    private val getLocalLiveStreamUseCase: GetLocalLiveStreamUseCase
) : ViewModel() {

    private val _stream: MutableStateFlow<Stream?> = MutableStateFlow(null)
    val stream = _stream.asStateFlow()

    fun setStreamId(id: Int) {
        getStream(id)
    }

    private fun getStream(id: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            getLocalLiveStreamUseCase.launch(GetLocalStreamParams(id)).collectLatest {
                _stream.tryEmit(it)
            }
        }
    }
}
