package org.rfcx.incidents.view.events

import android.content.Context
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
import org.rfcx.incidents.domain.GetProjectUseCase
import org.rfcx.incidents.domain.GetProjectsParams
import org.rfcx.incidents.domain.GetProjectsUseCase
import org.rfcx.incidents.domain.GetStreamsParams
import org.rfcx.incidents.domain.GetStreamsUseCase
import org.rfcx.incidents.entity.Stream
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.entity.project.Project
import org.rfcx.incidents.util.Preferences
import org.rfcx.incidents.util.asLiveData
import org.rfcx.incidents.util.isNetworkAvailable

class EventsViewModel(
    private val context: Context,
    private val getProjectUseCase: GetProjectUseCase,
    private val getProjectsUseCase: GetProjectsUseCase,
    private val getStreamsUseCase: GetStreamsUseCase,
    private val trackingDb: TrackingDb
) : ViewModel() {

    private val _projects = MutableLiveData<Result<List<Project>>>()
    val projects: LiveData<Result<List<Project>>> get() = _projects

    private val _selectedProjectId = MutableLiveData<Int>()
    val selectedProject = MediatorLiveData<Result<Project>>()

    private val _streams = MutableLiveData<Result<List<Stream>>>()
    val streams: LiveData<Result<List<Stream>>> get() = _streams

    var isLoadingMore = false

    init {
        selectedProject.addSource(_projects) { result ->
            when (result) {
                is Result.Loading -> Result.Loading
                is Result.Error -> Result.Error(result.throwable)
                is Result.Success -> {
                    val project = result.data.find { project -> project.id == _selectedProjectId.value }
                    if (project != null) {
                        Result.Success(project)
                    } else {
                        Result.Error(Error())
                    }
                }
            }
        }
        selectedProject.addSource(_selectedProjectId) { result ->
            val projects = _projects.value
            when (projects) {
                is Result.Loading -> Result.Loading
                is Result.Error -> Result.Error(projects.throwable)
                is Result.Success -> {
                    val project = projects.data.find { project -> project.id == result }
                    if (project != null) {
                        Result.Success(project)
                    } else {
                        Result.Error(Error())
                    }
                }
                else -> Result.Error(Error())
            }
        }
        _selectedProjectId.value = Preferences.getInstance(context).getInt(Preferences.SELECTED_PROJECT, -1)
    }

    fun selectProject(id: Int) {
        val preferences = Preferences.getInstance(context)
        preferences.putInt(Preferences.SELECTED_PROJECT, id)
        _selectedProjectId.value = id
    }

    fun getSelectProjectId(): Int {
        return Preferences.getInstance(context).getInt(Preferences.SELECTED_PROJECT, -1)
    }

    fun refreshProjects(force: Boolean = false) {
        if (!context.isNetworkAvailable()) return

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

    fun refreshStreams(projectId: Int, force: Boolean = false) {
        if (!context.isNetworkAvailable()) return

        val project = getProjectUseCase.getProjectFromLocal(projectId) ?: return
        if (project.serverId == null) return

        getStreamsUseCase.execute(
            object : DisposableSingleObserver<List<Stream>>() {
                override fun onSuccess(t: List<Stream>) {
                    _streams.value = Result.Success(t)
                }

                override fun onError(e: Throwable) {
                    _streams.value = Result.Error(e)
                }
            },
            GetStreamsParams(project.serverId!!, force)
        )
    }

    fun loadMoreStreams() {
    }

    fun saveLastTimeToKnowTheCurrentLocation(context: Context, time: Long) {
        val preferences = Preferences.getInstance(context)
        preferences.putLong(Preferences.LATEST_CURRENT_LOCATION_TIME, time)
    }

    fun distance(lastLocation: Location, loc: Location): String =
        LatLng(loc.latitude, loc.longitude).distanceTo(LatLng(lastLocation.latitude, lastLocation.longitude)).toString()

    fun getTrackingFromLocal(): LiveData<List<Tracking>> {
        return Transformations.map(trackingDb.getAllResultsAsync().asLiveData()) { it }
    }
}
