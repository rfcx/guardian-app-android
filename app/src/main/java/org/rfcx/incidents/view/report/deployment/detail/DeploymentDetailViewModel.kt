package org.rfcx.incidents.view.report.deployment.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.GetLocalLiveStreamUseCase
import org.rfcx.incidents.domain.GetLocalStreamParams
import org.rfcx.incidents.domain.guardian.GetLocalDeploymentParams
import org.rfcx.incidents.domain.guardian.GetLocalLiveDeploymentUseCase
import org.rfcx.incidents.entity.stream.Stream

class DeploymentDetailViewModel(
    private val getLocalLiveStreamUseCase: GetLocalLiveStreamUseCase,
    private val getLocalLiveDeploymentUseCase: GetLocalLiveDeploymentUseCase
) : ViewModel() {

    private val _stream: MutableStateFlow<Stream?> = MutableStateFlow(null)
    val stream = _stream.asStateFlow()

    private val _images: MutableStateFlow<List<DeploymentImageView>> = MutableStateFlow(emptyList())
    val images = _images.asStateFlow()

    fun setStreamId(id: Int) {
        getStream(id)
    }

    private fun getStream(id: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            getLocalLiveStreamUseCase.launch(GetLocalStreamParams(id)).collectLatest { result ->
                Log.d("GuardianAppImage", "Stream update")
                _stream.tryEmit(result)
                if (result?.deployment != null) {
                    getDeployment(result.deployment!!.id)
                }
            }
        }
    }

    private fun getDeployment(id: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            getLocalLiveDeploymentUseCase.launch(GetLocalDeploymentParams(id)).collectLatest { result ->
                if (result != null) {
                    _images.tryEmit(result.images?.map { it.toDeploymentImageView() } ?: listOf())
                }
            }
        }
    }
}
