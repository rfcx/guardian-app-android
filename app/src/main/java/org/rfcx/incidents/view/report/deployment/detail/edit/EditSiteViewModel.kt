package org.rfcx.incidents.view.report.deployment.detail.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.domain.GetLocalStreamParams
import org.rfcx.incidents.domain.GetLocalStreamUseCase
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.latitudeCoordinates
import org.rfcx.incidents.util.longitudeCoordinates

class EditSiteViewModel(
    private val getLocalStreamUseCase: GetLocalStreamUseCase
) : ViewModel() {

    private val _stream: MutableStateFlow<Stream?> = MutableStateFlow(null)
    val stream = _stream.asStateFlow()

    private val _siteName: MutableStateFlow<String> = MutableStateFlow("")
    val siteName = _siteName.asStateFlow()

    private val _siteCoordinates: MutableStateFlow<String> = MutableStateFlow("")
    val siteCoordinates = _siteCoordinates.asStateFlow()

    private val _siteAltitude: MutableStateFlow<String> = MutableStateFlow("")
    val siteAltitude = _siteAltitude.asStateFlow()

    private var fromMapPicker = false

    fun getStreamById(id: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            getLocalStreamUseCase.launch(GetLocalStreamParams(id)).collectLatest { stream ->
                if (stream != null) {
                    _stream.tryEmit(stream)
                    _siteName.tryEmit(stream.name)
                    if (!fromMapPicker) {
                        _siteCoordinates.tryEmit("${stream.latitude.latitudeCoordinates()}, ${stream.longitude.longitudeCoordinates()}")
                    }
                    _siteAltitude.tryEmit(stream.altitude.toString())
                }
            }
        }
    }

    fun pickLatitudeLongitude(latitude: Double?, longitude: Double?) {
        if (latitude != 0.0 && longitude != 0.0) {
            fromMapPicker = true
            _siteCoordinates.tryEmit("${latitude.latitudeCoordinates()}, ${longitude.longitudeCoordinates()}")
        }
    }
}
