package org.rfcx.incidents.view.report.deployment

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.realm.kotlin.freeze
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
    private val getDeploymentsUseCase: GetDeploymentsUseCase,
    private val getLocalStreamsUseCase: GetLocalStreamsUseCase,
    private val preferences: Preferences,
    private val getLocalProjectUseCase: GetLocalProjectUseCase,
    private val deployDeploymentUseCase: DeployDeploymentUseCase,
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val _deployments: MutableStateFlow<List<Deployment>> = MutableStateFlow(emptyList())
    val deployments = _deployments.asStateFlow()

    private val _markers: MutableStateFlow<List<MapMarker>> = MutableStateFlow(emptyList())
    val markers = _markers.asStateFlow()

    private val _selectedProject: MutableStateFlow<String> = MutableStateFlow("")
    val selectedProject = _selectedProject.asStateFlow()

    private val _currentLocationState: MutableStateFlow<Location?> = MutableStateFlow(null)
    val currentLocationState = _currentLocationState.asStateFlow()

    private var currentFilter = FilterDeployment.ALL
    private var currentAllDeployments = listOf<Deployment>()
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
            combine(getDeploymentsUseCase.launch(), getLocalStreamsUseCase.launch(GetLocalStreamsParams(preferences.getString(Preferences.SELECTED_PROJECT)!!))) { dp, site ->
                currentAllDeployments = dp
                currentAllStreams = site
                filterWithDeployment(dp, site, currentFilter)
            }.collect()
        }
    }

    private fun filterWithDeployment(deployments: List<Deployment>, streams: List<Stream>, filter: FilterDeployment = FilterDeployment.ALL) {
        when (filter) {
            FilterDeployment.ALL -> {
                val tempDeployments = deployments.map { it.freeze<Deployment>() }
                _deployments.tryEmit(tempDeployments)
                _markers.tryEmit(tempDeployments.map { it.toMark() } + streams.map { it.toMark() })
            }
            FilterDeployment.SYNCED -> {
                val tempDeployments = deployments.map<Deployment, Deployment> { it.freeze() }.filter { it.syncState == SyncState.SENT.value }
                _deployments.tryEmit(tempDeployments)
                _markers.tryEmit(tempDeployments.map { it.toMark() } + streams.map { it.toMark() })
            }
            FilterDeployment.UNSYNCED -> {
                val tempDeployments = deployments.map<Deployment, Deployment> { it.freeze() }.filter { it.syncState == SyncState.UNSENT.value }
                _deployments.tryEmit(tempDeployments)
                _markers.tryEmit(tempDeployments.map { it.toMark() } + streams.map { it.toMark() })
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
        filterWithDeployment(currentAllDeployments, currentAllStreams, filter)
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

fun Deployment.toMark(): MapMarker.DeploymentMarker {
    val pinImage = "PIN_GREEN"

    val description = "deployed"

    return MapMarker.DeploymentMarker(
        id,
        stream?.name ?: "",
        stream?.longitude ?: 0.0,
        stream?.latitude ?: 0.0,
        pinImage,
        description,
        deploymentKey,
        createdAt,
        deployedAt
    )
}

fun Stream.toMark(): MapMarker.SiteMarker {
    return MapMarker.SiteMarker(id, name, latitude, longitude, altitude, "SITE_MARKER")
}
