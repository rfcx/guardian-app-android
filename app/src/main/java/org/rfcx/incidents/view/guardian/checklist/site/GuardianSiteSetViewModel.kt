package org.rfcx.incidents.view.guardian.checklist.site

import androidx.lifecycle.ViewModel
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.latitudeCoordinates
import org.rfcx.incidents.util.longitudeCoordinates
import org.rfcx.incidents.util.setFormatLabel

class GuardianSiteSetViewModel : ViewModel() {

    private val _coordinateState: MutableStateFlow<String> = MutableStateFlow("")
    val coordinateState = _coordinateState.asStateFlow()

    private val _altitudeState: MutableStateFlow<String> = MutableStateFlow("")
    val altitudeState = _altitudeState.asStateFlow()

    private val _siteState: MutableStateFlow<String> = MutableStateFlow("")
    val siteState = _siteState.asStateFlow()

    private val _projectState: MutableStateFlow<String> = MutableStateFlow("")
    val projectState = _projectState.asStateFlow()

    lateinit var site: Stream

    fun setSite(stream: Stream) {
        site = stream
        setStateFromSite()
    }

    private fun setStateFromSite() {
        _coordinateState.tryEmit("${site.latitude.latitudeCoordinates()}, ${site.longitude.longitudeCoordinates()}")
        _altitudeState.tryEmit(0.0.setFormatLabel())
        _siteState.tryEmit(site.name)
        _projectState.tryEmit(site.projectId)
    }

}
