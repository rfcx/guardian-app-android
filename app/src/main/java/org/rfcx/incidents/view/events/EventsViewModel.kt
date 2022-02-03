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
import org.rfcx.incidents.data.api.project.GetProjectsOptions
import org.rfcx.incidents.data.api.project.GetProjectsUseCase
import org.rfcx.incidents.data.api.streams.GetStreamsUseCase
import org.rfcx.incidents.data.api.streams.StreamResponse
import org.rfcx.incidents.data.remote.Result
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.entity.project.Project
import org.rfcx.incidents.localdb.TrackingDb
import org.rfcx.incidents.util.Preferences
import org.rfcx.incidents.util.asLiveData
import org.rfcx.incidents.util.isNetworkAvailable

class EventsViewModel(
    private val context: Context,
    private val getProjectsUseCase: GetProjectsUseCase,
    private val trackingDb: TrackingDb,
    private val getStreams: GetStreamsUseCase
) : ViewModel() {

    private val _projects = MutableLiveData<Result<List<Project>>>()
    val projects: LiveData<Result<List<Project>>> get() = _projects

    private val _selectedProjectId = MutableLiveData<Int>()
    val selectedProject = MediatorLiveData<Result<Project>>()

    private val _streams = MutableLiveData<Result<List<StreamResponse>>>()
    val streams: LiveData<Result<List<StreamResponse>>> get() = _streams

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

    fun refreshProjects() {
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
            GetProjectsOptions()
        )
    }

    fun refreshStreams() {
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
