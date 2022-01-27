package org.rfcx.incidents.view.events

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mapbox.mapboxsdk.geometry.LatLng
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.incidents.R
import org.rfcx.incidents.data.api.project.GetProjectsUseCase
import org.rfcx.incidents.data.api.project.ProjectResponse
import org.rfcx.incidents.data.api.project.ProjectsRequestFactory
import org.rfcx.incidents.data.api.streams.GetStreamsUseCase
import org.rfcx.incidents.data.api.streams.StreamResponse
import org.rfcx.incidents.data.local.ProjectDb
import org.rfcx.incidents.data.remote.Result
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.entity.project.Project
import org.rfcx.incidents.localdb.TrackingDb
import org.rfcx.incidents.util.Preferences
import org.rfcx.incidents.util.asLiveData
import org.rfcx.incidents.util.isNetworkAvailable


class EventsViewModel(private val context: Context, private val getProjects: GetProjectsUseCase, private val projectDb: ProjectDb, private val trackingDb: TrackingDb, private val getStreams: GetStreamsUseCase) : ViewModel() {
	
	private val _projects = MutableLiveData<Result<List<Project>>>()
	val projects: LiveData<Result<List<Project>>> get() = _projects
	
	private val _streams = MutableLiveData<Result<List<StreamResponse>>>()
	val streams: LiveData<Result<List<StreamResponse>>> get() = _streams

	var isLoadMore = false
	
	fun loadMoreEvents() {
		isLoadMore = true
	}
	
	fun getTrackingFromLocal(): LiveData<List<Tracking>> {
		return Transformations.map(trackingDb.getAllResultsAsync().asLiveData()) { it }
	}
	
	fun refreshProjects() {
		if (context.isNetworkAvailable()) {
			getProjects.execute(object : DisposableSingleObserver<List<ProjectResponse>>() {
				override fun onSuccess(t: List<ProjectResponse>) {
					t.map {
						projectDb.insertOrUpdate(it)
					}
					_projects.value = Result.Success(listOf())
				}
				
				override fun onError(e: Throwable) {
					_projects.value = Result.Error(e)
				}
			}, ProjectsRequestFactory())
		}
	}
	
	fun getProjects(): List<Project> {
		return projectDb.getProjects()
	}
	
	fun getProjectName(id: Int): String = getProject(id)?.name
			?: context.getString(R.string.all_projects)
	
	fun getProject(id: Int): Project? {
		return projectDb.getProjectById(id)
	}
	
	fun refreshStreams() {
	
	}
	
	fun saveLastTimeToKnowTheCurrentLocation(context: Context, time: Long) {
		val preferences = Preferences.getInstance(context)
		preferences.putLong(Preferences.LATEST_CURRENT_LOCATION_TIME, time)
	}
	
	fun setProjectSelected(id: Int) {
		val preferences = Preferences.getInstance(context)
		preferences.putInt(Preferences.SELECTED_PROJECT, id)
	}
	
	fun distance(lastLocation: Location, loc: Location): String = LatLng(loc.latitude, loc.longitude).distanceTo(LatLng(lastLocation.latitude, lastLocation.longitude)).toString()
	
}
