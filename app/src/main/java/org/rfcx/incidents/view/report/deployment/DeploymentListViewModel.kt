package org.rfcx.incidents.view.report.deployment

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.reactivex.observers.DisposableSingleObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.guardian.deploy.UnSyncedExistException
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.GetLocalProjectUseCase
import org.rfcx.incidents.domain.GetLocalProjectsParams
import org.rfcx.incidents.domain.GetLocalStreamsParams
import org.rfcx.incidents.domain.GetLocalStreamsUseCase
import org.rfcx.incidents.domain.GetProjectsParams
import org.rfcx.incidents.domain.GetProjectsUseCase
import org.rfcx.incidents.domain.guardian.deploy.DeployDeploymentUseCase
import org.rfcx.incidents.domain.guardian.deploy.DeploymentDeployParams
import org.rfcx.incidents.domain.guardian.deploy.GetStreamWithDeploymentAndIncidentParams
import org.rfcx.incidents.domain.guardian.deploy.GetStreamWithDeploymentParams
import org.rfcx.incidents.domain.guardian.deploy.GetStreamsWithDeploymentAndIncidentUseCase
import org.rfcx.incidents.domain.guardian.deploy.GetStreamsWithDeploymentUseCase
import org.rfcx.incidents.domain.guardian.deploy.UploadImagesParams
import org.rfcx.incidents.domain.guardian.deploy.UploadImagesUseCase
import org.rfcx.incidents.domain.guardian.registration.GetRegistrationUseCase
import org.rfcx.incidents.domain.guardian.registration.OnlineRegistrationParams
import org.rfcx.incidents.domain.guardian.registration.SendRegistrationOnlineUseCase
import org.rfcx.incidents.entity.guardian.registration.GuardianRegistration
import org.rfcx.incidents.entity.guardian.registration.toRequest
import org.rfcx.incidents.entity.response.SyncState
import org.rfcx.incidents.entity.stream.Project
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.util.location.LocationHelper
import java.util.Date

class DeploymentListViewModel(
    private val getLocalStreamsUseCase: GetLocalStreamsUseCase,
    private val preferences: Preferences,
    private val getProjectsUseCase: GetProjectsUseCase,
    private val getStreamsWithDeploymentUseCase: GetStreamsWithDeploymentUseCase,
    private val getStreamsWithDeploymentAndIncidentUseCase: GetStreamsWithDeploymentAndIncidentUseCase,
    private val getLocalProjectUseCase: GetLocalProjectUseCase,
    private val deployDeploymentUseCase: DeployDeploymentUseCase,
    private val uploadImagesUseCase: UploadImagesUseCase,
    private val getRegistrationUseCase: GetRegistrationUseCase,
    private val sendRegistrationOnlineUseCase: SendRegistrationOnlineUseCase,
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val _deployments = MutableSharedFlow<List<ListItem>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val deployments = _deployments.asSharedFlow()

    private val _markers = MutableSharedFlow<List<MapMarker>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val markers = _markers.asSharedFlow()

    private val _selectedProject: MutableStateFlow<String> = MutableStateFlow("")
    val selectedProject = _selectedProject.asStateFlow()

    private val _projects = MutableSharedFlow<Result<List<Project>>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val projects = _projects.asSharedFlow()

    private val _streams = MutableSharedFlow<Result<List<Stream>>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val streams = _streams.asSharedFlow()

    private val _uploadImageState = MutableSharedFlow<String>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val uploadImageState = _uploadImageState.asSharedFlow()

    private val _registerState = MutableSharedFlow<String>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val registerState = _registerState.asSharedFlow()

    private val _alertUnsynced = MutableSharedFlow<Boolean>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val alertUnsynced = _alertUnsynced.asSharedFlow()

    private val _currentLocationState: MutableStateFlow<Location?> = MutableStateFlow(null)
    val currentLocationState = _currentLocationState.asStateFlow()

    private val _noDeploymentVisibilityState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val noDeploymentVisibilityState = _noDeploymentVisibilityState.asStateFlow()

    private val _noDeploymentTextContent: MutableStateFlow<String> = MutableStateFlow("")
    val noDeploymentTextContent = _noDeploymentTextContent.asStateFlow()

    private val _unsyncedAlertState: MutableStateFlow<Int> = MutableStateFlow(0)
    val unsyncedAlertState = _unsyncedAlertState.asStateFlow()

    private var currentFilter = FilterDeployment.UNSYNCED
    private var currentAllStreams = listOf<Stream>()
    private var currentAllRegistration = listOf<GuardianRegistration>()
    private var selectedProjectId = ""

    var isLoadingMore = false
    private var isMapScreen = false

    init {
        getLocationChanged()
        getSelectedProject()
        fetchProject(false)
    }

    private fun getLocationChanged() {
        viewModelScope.launch {
            locationHelper.getFlowLocationChanged().collectLatest {
                _currentLocationState.tryEmit(it)
            }
        }
    }

    private fun getDeployments(projectId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            combine(getRegistrationUseCase.launch(), getLocalStreamsUseCase.launch(GetLocalStreamsParams(projectId))) { reg, site ->
                currentAllStreams = site
                currentAllRegistration = reg
                filterWithDeployment(site, reg, currentFilter)
                checkForUnSyncedWorks(site, reg)
            }.collect()
        }
    }

    private fun filterWithDeployment(streams: List<Stream>, registration: List<GuardianRegistration>, filter: FilterDeployment = FilterDeployment.ALL) {
        when (filter) {
            FilterDeployment.ALL -> {
                _noDeploymentTextContent.tryEmit("you don't have any deployments or registrations")
                val tempDeployments = streams.filter { it.deployment != null }
                if (!isLoadingMore && !isMapScreen) {
                    _noDeploymentVisibilityState.tryEmit(tempDeployments.isEmpty() && registration.isEmpty())
                } else {
                    _noDeploymentVisibilityState.tryEmit(false)
                }
                val tempListItem =
                    (tempDeployments.map { it.toDeploymentListItem() } + registration.map { it.toRegistrationListItem() }).sortedByDescending { it.date }
                _deployments.tryEmit(tempListItem)
                val tempStream = streams.filter { it.deployment == null }
                _markers.tryEmit(tempDeployments.map { it.toDeploymentPin() } + tempStream.map { it.toSitePin() })
            }
            FilterDeployment.SYNCED -> {
                _noDeploymentTextContent.tryEmit("you don't have any synced deployments or registrations")
                val tempDeployments = streams.filter { it.deployment != null }
                    .filter {
                        it.deployment!!.syncState ==
                            SyncState.SENT.value && it.deployment!!.images?.all { im -> im.syncState == SyncState.SENT.value } ?: false
                    }
                val tempRegistration = registration.filter { it.syncState == SyncState.SENT.value }
                if (!isLoadingMore && !isMapScreen) {
                    _noDeploymentVisibilityState.tryEmit(tempDeployments.isEmpty() && registration.isEmpty())
                } else {
                    _noDeploymentVisibilityState.tryEmit(false)
                }
                val tempListItem =
                    (tempDeployments.map { it.toDeploymentListItem() } + tempRegistration.map { it.toRegistrationListItem() }).sortedByDescending { it.date }
                _deployments.tryEmit(tempListItem)
                val tempStream = streams.filter { it.deployment == null }
                _markers.tryEmit(tempDeployments.map { it.toDeploymentPin() } + tempStream.map { it.toSitePin() })
            }
            FilterDeployment.UNSYNCED -> {
                _noDeploymentTextContent.tryEmit("you don't have any unsynced deployments or registrations")
                val tempDeployments = streams.filter { it.deployment != null }
                    .filter {
                        it.deployment!!.syncState ==
                            SyncState.UNSENT.value || it.deployment!!.images?.any { im -> im.syncState == SyncState.UNSENT.value } ?: false
                    }
                val tempRegistration = registration.filter { it.syncState == SyncState.UNSENT.value }
                if (!isLoadingMore && !isMapScreen) {
                    _noDeploymentVisibilityState.tryEmit(tempDeployments.isEmpty() && tempRegistration.isEmpty())
                } else {
                    _noDeploymentVisibilityState.tryEmit(false)
                }
                val tempListItem =
                    (tempDeployments.map { it.toDeploymentListItem() } + tempRegistration.map { it.toRegistrationListItem() }).sortedByDescending { it.date }
                _deployments.tryEmit(tempListItem)
                val tempStream = streams.filter { it.deployment == null }
                _markers.tryEmit(tempDeployments.map { it.toDeploymentPin() } + tempStream.map { it.toSitePin() })
            }
        }
    }

    private fun checkForUnSyncedWorks(streams: List<Stream>, registration: List<GuardianRegistration>) {
        val tempDeployments = streams.filter { it.deployment != null }
            .filter {
                it.deployment!!.syncState ==
                    SyncState.UNSENT.value || it.deployment!!.images?.any { im -> im.syncState == SyncState.UNSENT.value } ?: false
            }
        val tempRegistration = registration.filter { it.syncState == SyncState.UNSENT.value }
        _unsyncedAlertState.tryEmit(tempDeployments.size + tempRegistration.size)
    }

    fun fetchProject(force: Boolean = false) {
        getProjectsUseCase.execute(
            object : DisposableSingleObserver<List<Project>>() {
                override fun onSuccess(t: List<Project>) {
                    _projects.tryEmit(Result.Success(t))
                }

                override fun onError(e: Throwable) {
                    _projects.tryEmit(Result.Error(e))
                }
            },
            GetProjectsParams(force)
        )
    }

    fun fetchFreshStreams(projectId: String? = null, force: Boolean = false, offset: Int = 0, fromAlertUnsynced: Boolean = false) {
        val tempProjectId = projectId ?: selectedProjectId
        isLoadingMore = true
        viewModelScope.launch(Dispatchers.Main) {
            getStreamsWithDeploymentAndIncidentUseCase.launch(
                GetStreamWithDeploymentAndIncidentParams(
                    projectId = tempProjectId, offset = offset, forceRefresh = force, fromAlertUnsynced = fromAlertUnsynced
                )
            ).collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        isLoadingMore = false
                        if (force) {
                            if (result.throwable is UnSyncedExistException) {
                                _alertUnsynced.tryEmit(true)
                            }
                        } else {
                            _streams.tryEmit(Result.Error(result.throwable))
                        }
                    }
                    Result.Loading -> _streams.tryEmit(Result.Loading)
                    is Result.Success -> {
                        isLoadingMore = false
                        _streams.tryEmit(Result.Success(result.data))
                    }
                }
            }
        }
    }

    fun fetchStream(projectId: String? = null, force: Boolean = false, swipeRefresh: Boolean = false, offset: Int = 0) {
        isLoadingMore = true
        viewModelScope.launch(Dispatchers.Main) {
            val tempProjectId = projectId ?: selectedProjectId
            getStreamsWithDeploymentUseCase.launch(
                GetStreamWithDeploymentParams(
                    projectId = tempProjectId, forceRefresh = force, offset = offset, streamRefresh = swipeRefresh
                )
            ).collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        isLoadingMore = false
                        _streams.tryEmit(Result.Error(result.throwable))
                    }
                    Result.Loading -> _streams.tryEmit(Result.Loading)
                    is Result.Success -> {
                        isLoadingMore = false
                        _streams.tryEmit(Result.Success(result.data))
                    }
                }
            }
        }
    }

    private fun getSelectedProject() {
        viewModelScope.launch(Dispatchers.Main) {
            preferences.getFlowForKey(Preferences.SELECTED_PROJECT).collectLatest { projectId ->
                getLocalProjectUseCase.launch(GetLocalProjectsParams(projectId)).collectLatest { project ->
                    project?.let {
                        selectedProjectId = it.id
                        _selectedProject.tryEmit(it.name)
                        // fetch streams after project changed
                        fetchFreshStreams(projectId = projectId)
                        // fetch deployments after project changed
                        getDeployments(it.id)
                    }
                }
            }
        }
    }

    fun setSelectedProject(projectId: String) {
        preferences.putString(Preferences.SELECTED_PROJECT, projectId)
    }

    fun addFilter(filter: FilterDeployment) {
        currentFilter = filter
        filterWithDeployment(currentAllStreams, currentAllRegistration, filter)
    }

    fun setScreen(isMapScreen: Boolean) {
        this.isMapScreen = isMapScreen
    }

    fun syncDeployment(id: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            deployDeploymentUseCase.launch(DeploymentDeployParams(id)).collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        // show error
                    }
                    Result.Loading -> {
                        // show loading
                    }
                    is Result.Success -> {
                        uploadImages(result.data)
                    }
                }
            }
        }
    }

    fun uploadImages(deploymentId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            uploadImagesUseCase.launch(UploadImagesParams(deploymentId)).collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        _uploadImageState.tryEmit(result.throwable.message ?: "")
                    }
                    Result.Loading -> {
                        // show loading
                    }
                    is Result.Success -> {
                        // show success
                    }
                }
            }
        }
    }

    fun register(registration: GuardianRegistration) {
        viewModelScope.launch {
            sendRegistrationOnlineUseCase.launch(OnlineRegistrationParams(registration.env, registration.toRequest())).collectLatest { result ->
                when (result) {
                    is Result.Error -> _registerState.tryEmit(result.throwable.message ?: "")
                    Result.Loading -> {}
                    is Result.Success -> {}
                }
            }
        }
    }

    enum class FilterDeployment {
        ALL, SYNCED, UNSYNCED
    }
}

sealed class ListItem {

    open val date: Date = Date()

    data class DeploymentListItem(
        val stream: Stream,
        val guardianId: String?,
        val guardianType: String?,
        override val date: Date
    ) : ListItem()

    data class RegistrationItem(
        val registration: GuardianRegistration,
        val guardianId: String,
        override val date: Date
    ) : ListItem()
}

fun Stream.toDeploymentListItem(): ListItem.DeploymentListItem {
    val gson = Gson()
    val params = this.deployment?.deviceParameters
    var guid = ""
    var type: String? = null
    if (params != null && params != "null") {
        val json = gson.fromJson(params, JsonObject::class.java)
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
    return ListItem.DeploymentListItem(
        this, guid, type, this.deployment?.deployedAt ?: Date()
    )
}

fun GuardianRegistration.toRegistrationListItem(): ListItem.RegistrationItem {
    return ListItem.RegistrationItem(
        this, this.guid, this.createdAt
    )
}

fun Stream.toDeploymentPin(): MapMarker.DeploymentMarker {
    val pinImage = "PIN_GREEN"

    val description = "deployed"

    val gson = Gson()
    val params = this.deployment?.deviceParameters
    var guid = ""
    var type: String? = null
    if (params != null && params != "null") {
        val json = gson.fromJson(params, JsonObject::class.java)
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
        id, name, longitude, latitude, pinImage, description, deployment!!.deploymentKey, deployment!!.createdAt, deployment!!.deployedAt, guid, type
    )
}

fun Stream.toSitePin(): MapMarker.SiteMarker {
    return MapMarker.SiteMarker(id, name, latitude, longitude, altitude, "SITE_MARKER")
}
