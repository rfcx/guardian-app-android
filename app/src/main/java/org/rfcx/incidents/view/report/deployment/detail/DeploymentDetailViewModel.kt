package org.rfcx.incidents.view.report.deployment.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.GetLocalStreamParams
import org.rfcx.incidents.domain.GetLocalStreamUseCase
import org.rfcx.incidents.domain.GetLocalStreamsUseCase
import org.rfcx.incidents.entity.stream.Stream

class DeploymentDetailViewModel(
    private val getLocalStreamUseCase: GetLocalStreamUseCase
) : ViewModel() {

    private val _stream: MutableStateFlow<Stream?> = MutableStateFlow(null)
    val stream = _stream.asStateFlow()

    fun setStreamId(id: String) {
        getStream(id)
    }

    private fun getStream(id: String) {
        viewModelScope.launch(Dispatchers.Main) {
            getLocalStreamUseCase.launch(GetLocalStreamParams(id)).collectLatest {
                _stream.tryEmit(it)
            }
        }
    }
}