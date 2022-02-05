package org.rfcx.incidents.view.events

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mapbox.mapboxsdk.geometry.LatLng
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.incidents.data.local.TrackingDb
import org.rfcx.incidents.data.remote.common.Result
import org.rfcx.incidents.domain.GetProjectsParams
import org.rfcx.incidents.domain.GetProjectsUseCase
import org.rfcx.incidents.domain.GetStreamsParams
import org.rfcx.incidents.domain.GetStreamsUseCase
import org.rfcx.incidents.entity.Stream
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.entity.project.Project
import org.rfcx.incidents.util.Preferences
import org.rfcx.incidents.util.asLiveData

class EventsViewModel(
    private val preferences: Preferences,
    private val getProjectsUseCase: GetProjectsUseCase,
    private val getStreamsUseCase: GetStreamsUseCase,
    private val trackingDb: TrackingDb
) : ViewModel() {

    private val _projects = MutableLiveData<Result<List<Project>>>()
    val projects: LiveData<Result<List<Project>>> get() = _projects

    private val _selectedProjectId = MutableLiveData<Int>()
    val selectedProject = MediatorLiveData<Result<Project>>()

    private val _streams = MutableLiveData<Result<List<Stream>>>()
    val streams = MediatorLiveData<Result<List<Stream>>>()

    var isLoadingMore = false

    init {
        // When the selected project changes, refresh the streams
        streams.addSource(selectedProject) { result ->
            if (result is Result.Success) {
                refreshStreams()
            }
        }
        streams.addSource(_streams) { result -> streams.value = result }
        // When the projects or selected project id change, reload the selected project
        selectedProject.addSource(_projects) { result ->
            selectedProject.value = findProject(_selectedProjectId.value ?: -1, result)
        }
        selectedProject.addSource(_selectedProjectId) { id ->
            projects.value?.let { result -> selectedProject.value = findProject(id, result) }
        }
        _selectedProjectId.value = preferences.getInt(Preferences.SELECTED_PROJECT, -1)
    }

    fun selectProject(id: Int) {
        preferences.putInt(Preferences.SELECTED_PROJECT, id)
        _selectedProjectId.value = id
    }

    private fun findProject(id: Int, result: Result<List<Project>>) = when (result) {
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

    fun refreshStreams(force: Boolean = false, more: Boolean = false) {
        val projectId = selectedProject.value?.let { if (it is Result.Success) it.data.serverId else null } ?: return
        getStreamsUseCase.execute(
            object : DisposableSingleObserver<List<Stream>>() {
                override fun onSuccess(t: List<Stream>) {
                    _streams.value = Result.Success(t)
                }

                override fun onError(e: Throwable) {
                    _streams.value = Result.Error(e)
                }
            },
            GetStreamsParams(projectId, force, more)
        )
    }

    fun saveLastTimeToKnowTheCurrentLocation(time: Long) {
        preferences.putLong(Preferences.LATEST_CURRENT_LOCATION_TIME, time)
    }

    fun distance(lastLocation: Location, loc: Location): String =
        LatLng(loc.latitude, loc.longitude).distanceTo(LatLng(lastLocation.latitude, lastLocation.longitude)).toString()

    fun getTrackingFromLocal(): LiveData<List<Tracking>> {
        return Transformations.map(trackingDb.getAllResultsAsync().asLiveData()) { it }
    }
}
