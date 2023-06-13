package org.rfcx.incidents.view.report.deployment.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.GetLocalLiveStreamUseCase
import org.rfcx.incidents.domain.GetLocalStreamParams
import org.rfcx.incidents.domain.guardian.GetDeploymentImagesParams
import org.rfcx.incidents.domain.guardian.GetDeploymentImagesUseCase
import org.rfcx.incidents.domain.guardian.GetLocalDeploymentParams
import org.rfcx.incidents.domain.guardian.GetLocalLiveDeploymentUseCase
import org.rfcx.incidents.entity.stream.Stream

class DeploymentDetailViewModel(
    private val getLocalLiveStreamUseCase: GetLocalLiveStreamUseCase,
    private val getLocalLiveDeploymentUseCase: GetLocalLiveDeploymentUseCase,
    private val getDeploymentImagesUseCase: GetDeploymentImagesUseCase,
) : ViewModel() {

    private val _stream: MutableStateFlow<Stream?> = MutableStateFlow(null)
    val stream = _stream.asStateFlow()

    private val _images: MutableStateFlow<List<DeploymentImageView>> = MutableStateFlow(emptyList())
    val images = _images.asStateFlow()

    private val _errorFetching: MutableStateFlow<String?> = MutableStateFlow(null)
    val errorFetching = _errorFetching.asStateFlow()

    fun setStreamId(id: Int) {
        getStream(id)
    }

    private fun getStream(id: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            getLocalLiveStreamUseCase.launch(GetLocalStreamParams(id)).collectLatest { result ->
                _stream.tryEmit(result)
                if (result?.deployment != null) {
                    getDeployment(result.deployment!!.id)
                    getImages(result.deployment!!.id)
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

    private fun getImages(deploymentId: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            getDeploymentImagesUseCase.launch(GetDeploymentImagesParams(deploymentId)).collectLatest { result ->
                // only expect error
                // already get notify changed from deployment
                if (result is Result.Error) {
                    _errorFetching.tryEmit(result.throwable.message)
                }
            }
        }
    }
}
