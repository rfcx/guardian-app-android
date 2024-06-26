package org.rfcx.incidents.view.report.deployment.detail.image

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.GetLocalLiveStreamUseCase
import org.rfcx.incidents.domain.GetLocalStreamParams
import org.rfcx.incidents.domain.guardian.detail.AddImageParams
import org.rfcx.incidents.domain.guardian.detail.AddImageToDeploymentUseCase
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.view.guardian.checklist.photos.Image
import org.rfcx.incidents.view.report.deployment.detail.toImage

class AddImageViewModel(
    private val getLocalLiveStreamUseCase: GetLocalLiveStreamUseCase,
    private val addImageToDeploymentUseCase: AddImageToDeploymentUseCase
) : ViewModel() {

    private val _stream: MutableStateFlow<Stream?> = MutableStateFlow(null)
    val stream = _stream.asStateFlow()

    private val _images: MutableStateFlow<List<Image>> = MutableStateFlow(emptyList())
    val images = _images.asStateFlow()

    fun setStreamId(id: Int) {
        getStream(id)
    }

    fun saveImages(images: List<Image>) {
        val id = _stream.value?.deployment?.id
        id?.let {
            addImageToDeploymentUseCase.launch(AddImageParams(it, images.filter { im -> im.path != null }))
        }
    }

    private fun getStream(id: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            getLocalLiveStreamUseCase.launch(GetLocalStreamParams(id)).collectLatest { result ->
                _stream.tryEmit(result)

                if (result?.deployment?.images != null) {
                    _images.tryEmit(result.deployment!!.images!!.map { it.toImage() })
                }
            }
        }
    }
}
