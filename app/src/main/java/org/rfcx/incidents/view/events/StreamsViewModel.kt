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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.rfcx.incidents.data.local.TrackingDb
import org.rfcx.incidents.data.local.realm.asLiveData
import org.rfcx.incidents.data.preferences.Preferences
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.GetProjectsParams
import org.rfcx.incidents.domain.GetProjectsUseCase
import org.rfcx.incidents.domain.GetStreamsParams
import org.rfcx.incidents.domain.GetStreamsWithIncidentUseCase
import org.rfcx.incidents.domain.guardian.deploy.GetStreamWithDeploymentAndIncidentParams
import org.rfcx.incidents.domain.guardian.deploy.GetStreamsWithDeploymentAndIncidentUseCase
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.entity.stream.Project
import org.rfcx.incidents.entity.stream.Stream

class StreamsViewModel(
    private val preferences: Preferences,
    private val getProjectsUseCase: GetProjectsUseCase,
    private val getStreamsWithIncidentUseCase: GetStreamsWithIncidentUseCase,
    private val getStreamsWithDeploymentAndIncidentUseCase: GetStreamsWithDeploymentAndIncidentUseCase,
    private val trackingDb: TrackingDb
) : ViewModel() {

    private val _projects = MutableLiveData<Result<List<Project>>>()
    val projects: LiveData<Result<List<Project>>> get() = _projects

    private val _selectedProjectId = MutableLiveData<String>()
    val selectedProject = MediatorLiveData<Result<Project>>()

    private val _streams = MutableLiveData<Result<List<Stream>>>()
    val streams = MediatorLiveData<Result<List<Stream>>>()

    var isLoadingMore = false

    init {
        // When the selected project changes, refresh the streams
        streams.addSource(selectedProject) { result ->
            if (result is Result.Success) {
                fetchFreshStreams()
            }
        }
        streams.addSource(_streams) { result -> streams.value = result }
        // When the projects or selected project id change, reload the selected project
        selectedProject.addSource(_projects) { result ->
            selectedProject.value = findProject(_selectedProjectId.value ?: "", result)
        }
        selectedProject.addSource(_selectedProjectId) { id ->
            projects.value?.let { result -> selectedProject.value = findProject(id, result) }
        }
        _selectedProjectId.value = preferences.getString(Preferences.SELECTED_PROJECT, "")
    }

    fun selectProject(id: String) {
        preferences.putString(Preferences.SELECTED_PROJECT, id)
        _selectedProjectId.value = id
    }

    private fun findProject(id: String, result: Result<List<Project>>) = when (result) {
        is Result.Loading -> Result.Loading
        is Result.Error -> Result.Error(result.throwable)
        is Result.Success -> result.data.find { project -> project.id == id }?.let { Result.Success(it) } ?: Result.Error(Error("Not found"))
    }

    fun refreshProjects(force: Boolean = false) {
        getProjectsUseCase.execute(
            object : DisposableSingleObserver<List<Project>>() {
                override fun onSuccess(t: List<Project>) {
                    _projects.value = Result.Success(t)
                }

                override fun onError(e: Throwable) {
                    _projects.value = Result.Error(e)
                }
            },
            GetProjectsParams(force)
        )
    }
    fun fetchFreshStreams(force: Boolean = false, offset: Int = 0) {
        val projectId = selectedProject.value?.let { if (it is Result.Success) it.data.id else null } ?: return
        viewModelScope.launch(Dispatchers.Main) {
            getStreamsWithDeploymentAndIncidentUseCase.launch(GetStreamWithDeploymentAndIncidentParams(
                projectId = projectId,
                offset = offset,
                forceRefresh = force
            )).collectLatest { result ->
                when(result) {
                    is Result.Error -> {
                        isLoadingMore = false
                        _streams.value = Result.Error(result.throwable)
                    }
                    Result.Loading -> {}
                    is Result.Success -> {
                        isLoadingMore = false
                        _streams.value = Result.Success(result.data)
                    }
                }
            }
        }
    }

    fun refreshStreams(force: Boolean = false, offset: Int = 0, streamRefresh: Boolean = false) {
        isLoadingMore = true
        val projectId = selectedProject.value?.let { if (it is Result.Success) it.data.id else null } ?: return
        getStreamsWithIncidentUseCase.execute(
            object : DisposableSingleObserver<List<Stream>>() {
                override fun onSuccess(t: List<Stream>) {
                    isLoadingMore = false
                    _streams.value = Result.Success(t)
                }

                override fun onError(e: Throwable) {
                    isLoadingMore = false
                    _streams.value = Result.Error(e)
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
