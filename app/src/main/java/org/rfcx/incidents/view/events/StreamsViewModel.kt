package org.rfcx.incidents.view.events

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.observers.DisposableSingleObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.local.TrackingDb
import org.rfcx.incidents.data.local.realm.asLiveData
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.GetLocalProjectUseCase
import org.rfcx.incidents.domain.GetLocalProjectsParams
import org.rfcx.incidents.domain.GetLocalStreamsParams
import org.rfcx.incidents.domain.GetLocalStreamsUseCase
import org.rfcx.incidents.domain.GetProjectsParams
import org.rfcx.incidents.domain.GetProjectsUseCase
import org.rfcx.incidents.domain.GetStreamsParams
import org.rfcx.incidents.domain.GetStreamsWithIncidentUseCase
import org.rfcx.incidents.domain.guardian.deploy.GetStreamWithDeploymentAndIncidentParams
import org.rfcx.incidents.domain.guardian.deploy.GetStreamsWithDeploymentAndIncidentUseCase
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.entity.stream.Project
import org.rfcx.incidents.entity.stream.Stream
import org.rfcx.incidents.view.report.deployment.ListItem

class StreamsViewModel(
    private val preferences: Preferences,
    private val getProjectsUseCase: GetProjectsUseCase,
    private val getStreamsWithIncidentUseCase: GetStreamsWithIncidentUseCase,
    private val getStreamsWithDeploymentAndIncidentUseCase: GetStreamsWithDeploymentAndIncidentUseCase,
    private val trackingDb: TrackingDb,
    private val getLocalProjectUseCase: GetLocalProjectUseCase,
    private val getLocalStreamsUseCase: GetLocalStreamsUseCase
) : ViewModel() {

    private val _projects = MutableSharedFlow<Result<List<Project>>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val projects = _projects.asSharedFlow()

    private val _selectedProject: MutableStateFlow<String> = MutableStateFlow("")
    val selectedProject = _selectedProject.asStateFlow()

    private val _streams = MutableSharedFlow<Result<List<Stream>>>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val streams = _streams.asSharedFlow()

    var isLoadingMore = false
    private var selectedProjectId = ""

    init {
        getSelectedProject()
        refreshProjects(false)
    }

    private fun getSelectedProject() {
        Log.d("Guardian", "Get Project changed")
        viewModelScope.launch(Dispatchers.Main) {
            preferences.getFlowForKey(Preferences.SELECTED_PROJECT).collectLatest { projectId ->
                getLocalProjectUseCase.launch(GetLocalProjectsParams(projectId)).collectLatest { project ->
                    project?.let {
                        Log.d("Guardian", it.name)
                        selectedProjectId = it.id
                        _selectedProject.tryEmit(it.name)
                        // fetch streams after project changed
                        Log.d("Guardian", "project changed")
                        fetchFreshStreams()
                        // fetch incidents after project changed
                        getIncidents(it.id)
                    }
                }
            }
        }
    }

    fun selectProject(id: String) {
        preferences.putString(Preferences.SELECTED_PROJECT, id)
    }

    private fun getIncidents(projectId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            getLocalStreamsUseCase.launch(GetLocalStreamsParams(projectId)).collectLatest { result ->
                Log.d("GuardianApp", "get incident $result")
                _streams.tryEmit(Result.Success(result))
            }
        }
    }

    fun refreshProjects(force: Boolean = false) {
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

    fun fetchFreshStreams(force: Boolean = false, offset: Int = 0) {
        Log.d("Guardian", "here 2")
        val projectId = selectedProjectId
        viewModelScope.launch(Dispatchers.Main) {
            getStreamsWithDeploymentAndIncidentUseCase.launch(
                GetStreamWithDeploymentAndIncidentParams(
                    projectId = projectId,
                    offset = offset,
                    forceRefresh = force
                )
            ).collectLatest { result ->
                when (result) {
                    is Result.Error -> {
                        isLoadingMore = false
                        _streams.tryEmit(result)
                    }
                    Result.Loading -> {}
                    is Result.Success -> {
                        isLoadingMore = false
                        _streams.tryEmit(result)
                    }
                }
            }
        }
    }

    fun refreshStreams(force: Boolean = false, offset: Int = 0, streamRefresh: Boolean = false) {
        Log.d("Guardian", "here 1")
        isLoadingMore = true
        val projectId = selectedProjectId
        getStreamsWithIncidentUseCase.execute(
            object : DisposableSingleObserver<List<Stream>>() {
                override fun onSuccess(t: List<Stream>) {
                    isLoadingMore = false
                    _streams.tryEmit(Result.Success(t))
                }

                override fun onError(e: Throwable) {
                    isLoadingMore = false
                    _streams.tryEmit(Result.Error(e))
                }
            },
            GetStreamsParams(projectId, force, offset, streamRefresh)
        )
    }

    fun saveLastTimeToKnowTheCurrentLocation(time: Long) {
        preferences.putLong(Preferences.LATEST_CURRENT_LOCATION_TIME, time)
    }

    fun getTrackingFromLocal(): LiveData<List<Tracking>> {
        return Transformations.map(trackingDb.getAllResultsAsync().asLiveData()) { it }
    }
}
