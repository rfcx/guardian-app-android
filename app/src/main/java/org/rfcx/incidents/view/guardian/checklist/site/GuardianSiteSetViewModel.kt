package org.rfcx.incidents.view.guardian.checklist.site

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.domain.GetLocalProjectUseCase
import org.rfcx.incidents.domain.GetLocalProjectsParams
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.latitudeCoordinates
import org.rfcx.incidents.util.location.LocationHelper
import org.rfcx.incidents.util.longitudeCoordinates
import org.rfcx.incidents.util.setFormatLabel

class GuardianSiteSetViewModel(
    private val locationHelper: LocationHelper,
    private val getLocalProjectUseCase: GetLocalProjectUseCase,
    private val preferences: Preferences
    ) : ViewModel() {

    private val _coordinateState: MutableStateFlow<String> = MutableStateFlow("")
    val coordinateState = _coordinateState.asStateFlow()

    private val _altitudeState: MutableStateFlow<String> = MutableStateFlow("")
    val altitudeState = _altitudeState.asStateFlow()

    private val _siteState: MutableStateFlow<String> = MutableStateFlow("")
    val siteState = _siteState.asStateFlow()

    private val _projectState: MutableStateFlow<String> = MutableStateFlow("")
    val projectState = _projectState.asStateFlow()

    private val _currentLocationState: MutableStateFlow<Location?> = MutableStateFlow(null)
    val currentLocationState = _currentLocationState.asStateFlow()

    private lateinit var site: Stream
    private var isNewSite = false

    init {
        getLocationChanged()
    }

    fun setSite(stream: Stream) {
        site = stream
        setStateFromSite()
        getProjectName()
    }

    fun setIsNewSite(value: Boolean) {
        isNewSite = value
    }

    fun updateSiteToCurrentLocation() {
        val loc = _currentLocationState.value
        loc?.let {
            site.latitude = it.latitude
            site.longitude = it.longitude
            site.altitude = it.altitude
        }
        setStateFromSite()
    }

    private fun getLocationChanged() {
        viewModelScope.launch {
            locationHelper.getFlowLocationChanged().collectLatest {
                _currentLocationState.tryEmit(it)
                if (isNewSite) {
                    updateSiteToCurrentLocation()
                }
                it?.altitude?.let { al ->
                    _altitudeState.tryEmit(al.setFormatLabel())
                }
            }
        }
    }

    private fun getProjectName() {
        val selectedProject = preferences.getString(Preferences.SELECTED_PROJECT) ?: ""
        viewModelScope.launch {
            getLocalProjectUseCase.launch(GetLocalProjectsParams(selectedProject)).collectLatest { project ->
                project?.let {
                    _projectState.tryEmit(it.name)
                }
            }
        }
    }

    private fun setStateFromSite() {
        _coordinateState.tryEmit("${site.latitude.latitudeCoordinates()}, ${site.longitude.longitudeCoordinates()}")
        _siteState.tryEmit(site.name)
    }

}
