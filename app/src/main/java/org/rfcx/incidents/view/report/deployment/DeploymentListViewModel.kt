package org.rfcx.incidents.view.report.deployment

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.GetLocalProjectUseCase
import org.rfcx.incidents.domain.GetLocalProjectsParams
import org.rfcx.incidents.domain.GetLocalStreamsParams
import org.rfcx.incidents.domain.GetLocalStreamsUseCase
import org.rfcx.incidents.domain.guardian.deploy.DeployDeploymentUseCase
import org.rfcx.incidents.domain.guardian.deploy.DeploymentDeployParams
import org.rfcx.incidents.domain.guardian.deploy.GetDeploymentsUseCase
import org.rfcx.incidents.entity.guardian.deployment.Deployment
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.location.LocationHelper

class DeploymentListViewModel(
    private val getLocalStreamsUseCase: GetLocalStreamsUseCase,
    private val preferences: Preferences,
    private val getLocalProjectUseCase: GetLocalProjectUseCase,
    private val deployDeploymentUseCase: DeployDeploymentUseCase,
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val _deployments: MutableStateFlow<List<Stream>> = MutableStateFlow(emptyList())
    val deployments = _deployments.asStateFlow()

    private val _markers: MutableStateFlow<List<MapMarker>> = MutableStateFlow(emptyList())
    val markers = _markers.asStateFlow()

    private val _selectedProject: MutableStateFlow<String> = MutableStateFlow("")
    val selectedProject = _selectedProject.asStateFlow()

    private val _currentLocationState: MutableStateFlow<Location?> = MutableStateFlow(null)
    val currentLocationState = _currentLocationState.asStateFlow()

    private var currentFilter = FilterDeployment.ALL
    private var currentAllStreams = listOf<Stream>()

    init {
        getLocationChanged()
        getDeployments()
        getSelectedProject()
    }

    private fun getLocationChanged() {
        viewModelScope.launch {
            locationHelper.getFlowLocationChanged().collectLatest {
                _currentLocationState.tryEmit(it)
            }
        }
    }

    private fun getDeployments() {
        viewModelScope.launch(Dispatchers.Main) {
            getLocalStreamsUseCase.launch(GetLocalStreamsParams(preferences.getString(Preferences.SELECTED_PROJECT)!!)).collectLatest { site ->
                currentAllStreams = site
                filterWithDeployment(site, currentFilter)
            }
        }
    }

    private fun filterWithDeployment(streams: List<Stream>, filter: FilterDeployment = FilterDeployment.ALL) {
        when (filter) {
            FilterDeployment.ALL -> {
                val tempDeployments = streams.filter { it.deployment != null }
                Log.d("GuardianApp", "${tempDeployments.size}")
                _deployments.tryEmit(tempDeployments)
                val tempStream = streams.filter { it.deployment == null }
                _markers.tryEmit(tempDeployments.map { it.toDeploymentPin() } + tempStream.map { it.toSitePin() })
            }
            FilterDeployment.SYNCED -> {
                val tempDeployments = streams.filter { it.deployment != null }.filter { it.deployment!!.syncState == SyncState.SENT.value }
                _deployments.tryEmit(tempDeployments)
                val tempStream = streams.filter { it.deployment == null }
                _markers.tryEmit(tempDeployments.map { it.toDeploymentPin() } + tempStream.map { it.toSitePin() })
            }
            FilterDeployment.UNSYNCED -> {
                val tempDeployments = streams.filter { it.deployment != null }.filter { it.deployment!!.syncState == SyncState.UNSENT.value }
                _deployments.tryEmit(tempDeployments)
                val tempStream = streams.filter { it.deployment == null }
                _markers.tryEmit(tempDeployments.map { it.toDeploymentPin() } + tempStream.map { it.toSitePin() })
            }
        }
    }

    private fun getSelectedProject() {
        preferences.getString(Preferences.SELECTED_PROJECT)?.let { projectId ->
            viewModelScope.launch(Dispatchers.Main) {
                getLocalProjectUseCase.launch(GetLocalProjectsParams(projectId)).collectLatest { project ->
                    project?.let {
                        _selectedProject.tryEmit(it.name)
                    }
                }
            }
        }
    }

    fun addFilter(filter: FilterDeployment) {
        currentFilter = filter
        filterWithDeployment(currentAllStreams, filter)
    }

    fun syncDeployment(id: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            deployDeploymentUseCase.launch(DeploymentDeployParams(id)).collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        //show error
                    }
                    Result.Loading -> {
                        //show loading
                    }
                    is Result.Success -> {
                        //show success
                    }
                }
            }
        }
    }

    enum class FilterDeployment {
        ALL, SYNCED, UNSYNCED
    }
}

fun Stream.toDeploymentPin(): MapMarker.DeploymentMarker {
    val pinImage = "PIN_GREEN"

    val description = "deployed"

    Log.d("GuardianApp D", "$latitude")
    return MapMarker.DeploymentMarker(
        id,
        name,
        longitude,
        latitude,
        pinImage,
        description,
        deployment!!.deploymentKey,
        deployment!!.createdAt,
        deployment!!.deployedAt
    )
}

fun Stream.toSitePin(): MapMarker.SiteMarker {
    Log.d("GuardianApp S", "$latitude")
    return MapMarker.SiteMarker(id, name, latitude, longitude, altitude, "SITE_MARKER")
}
