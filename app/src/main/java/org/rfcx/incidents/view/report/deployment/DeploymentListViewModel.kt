package org.rfcx.incidents.view.report.deployment

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.GetLocalProjectUseCase
import org.rfcx.incidents.domain.GetLocalProjectsParams
import org.rfcx.incidents.domain.GetLocalStreamsParams
import org.rfcx.incidents.domain.GetLocalStreamsUseCase
import org.rfcx.incidents.domain.guardian.deploy.DeployDeploymentUseCase
import org.rfcx.incidents.domain.guardian.deploy.DeploymentDeployParams
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

    private val _deployments: MutableStateFlow<List<DeploymentListItem>> = MutableStateFlow(emptyList())
    val deployments = _deployments.asStateFlow()

    private val _markers: MutableStateFlow<List<MapMarker>> = MutableStateFlow(emptyList())
    val markers = _markers.asStateFlow()

    private val _selectedProject: MutableStateFlow<String> = MutableStateFlow("")
    val selectedProject = _selectedProject.asStateFlow()

    private val _currentLocationState: MutableStateFlow<Location?> = MutableStateFlow(null)
    val currentLocationState = _currentLocationState.asStateFlow()

    private val _noDeploymentVisibilityState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val noDeploymentVisibilityState = _noDeploymentVisibilityState.asStateFlow()

    private val _noDeploymentTextContent: MutableStateFlow<String> = MutableStateFlow("")
    val noDeploymentTextContent = _noDeploymentTextContent.asStateFlow()

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
                _noDeploymentTextContent.tryEmit("you don't have any deployments")
                val tempDeployments = streams.filter { it.deployment != null }
                _noDeploymentVisibilityState.tryEmit(tempDeployments.isEmpty())
                _deployments.tryEmit(tempDeployments.map { it.toDeploymentListItem() })
                val tempStream = streams.filter { it.deployment == null }
                _markers.tryEmit(tempDeployments.map { it.toDeploymentPin() } + tempStream.map { it.toSitePin() })
            }
            FilterDeployment.SYNCED -> {
                _noDeploymentTextContent.tryEmit("you don't have any synced deployments")
                val tempDeployments = streams.filter { it.deployment != null }.filter { it.deployment!!.syncState == SyncState.SENT.value }
                _noDeploymentVisibilityState.tryEmit(tempDeployments.isEmpty())
                _deployments.tryEmit(tempDeployments.map { it.toDeploymentListItem() })
                val tempStream = streams.filter { it.deployment == null }
                _markers.tryEmit(tempDeployments.map { it.toDeploymentPin() } + tempStream.map { it.toSitePin() })
            }
            FilterDeployment.UNSYNCED -> {
                _noDeploymentTextContent.tryEmit("you don't have any unsynced deployments")
                val tempDeployments = streams.filter { it.deployment != null }.filter { it.deployment!!.syncState == SyncState.UNSENT.value }
                _noDeploymentVisibilityState.tryEmit(tempDeployments.isEmpty())
                _deployments.tryEmit(tempDeployments.map { it.toDeploymentListItem() })
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

data class DeploymentListItem(
    val stream: Stream,
    val guardianId: String?,
    val guardianType: String?
)

fun Stream.toDeploymentListItem(): DeploymentListItem {
    val gson = Gson()
    val params = this.deployment?.deviceParameters
    var guid = ""
    var type: String? = null
    params?.let {
        val json = gson.fromJson(it, JsonObject::class.java)
        if (json.has("guid")) {
            guid = json.get("guid").asString
        }
        if (json.has("guardianType")) {
            when (json.get("guardianType").asString) {
                "CELL_ONLY" -> type = "Cell"
                "CELL_SMS" -> type = "Cell"
                "SAT_ONLY" -> type = "Sat"
                "OFFLINE_MODE" -> type = "Cell"
            }
        }
    }
    return DeploymentListItem(
        this,
        guid,
        type
    )
}

fun Stream.toDeploymentPin(): MapMarker.DeploymentMarker {
    val pinImage = "PIN_GREEN"

    val description = "deployed"

    val gson = Gson()
    val params = this.deployment?.deviceParameters
    var guid = ""
    var type: String? = null
    params?.let {
        val json = gson.fromJson(it, JsonObject::class.java)
        if (json.has("guid")) {
            guid = json.get("guid").asString
        }
        if (json.has("guardianType")) {
            when (json.get("guardianType").asString) {
                "CELL_ONLY" -> type = "Cell"
                "CELL_SMS" -> type = "Cell"
                "SAT_ONLY" -> type = "Sat"
                "OFFLINE_MODE" -> type = "Cell"
            }
        }
    }
    return MapMarker.DeploymentMarker(
        id,
        name,
        longitude,
        latitude,
        pinImage,
        description,
        deployment!!.deploymentKey,
        deployment!!.createdAt,
        deployment!!.deployedAt,
        guid,
        type
    )
}

fun Stream.toSitePin(): MapMarker.SiteMarker {
    return MapMarker.SiteMarker(id, name, latitude, longitude, altitude, "SITE_MARKER")
}
