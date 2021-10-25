package org.rfcx.ranger.view.events

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.mapbox.mapboxsdk.geometry.LatLng
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.R
import org.rfcx.ranger.data.api.events.GetEvents
import org.rfcx.ranger.data.api.events.ResponseEvent
import org.rfcx.ranger.data.api.project.GetProjectsUseCase
import org.rfcx.ranger.data.api.project.ProjectResponse
import org.rfcx.ranger.data.api.project.ProjectsRequestFactory
import org.rfcx.ranger.data.api.site.GetStreamsUseCase
import org.rfcx.ranger.data.api.site.StreamResponse
import org.rfcx.ranger.data.api.site.StreamsRequestFactory
import org.rfcx.ranger.data.local.AlertDb
import org.rfcx.ranger.data.local.ProjectDb
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.entity.Stream
import org.rfcx.ranger.entity.location.Tracking
import org.rfcx.ranger.entity.project.Project
import org.rfcx.ranger.localdb.StreamDb
import org.rfcx.ranger.localdb.TrackingDb
import org.rfcx.ranger.util.CloudMessaging
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.asLiveData
import org.rfcx.ranger.util.isNetworkAvailable
import org.rfcx.ranger.view.events.adapter.EventGroup


class EventsViewModel(private val context: Context, private val getProjects: GetProjectsUseCase, private val projectDb: ProjectDb, private val streamDb: StreamDb, private val trackingDb: TrackingDb, private val alertDb: AlertDb, private val getStreams: GetStreamsUseCase, private val getEvents: GetEvents) : ViewModel() {
	private val _projects = MutableLiveData<Result<List<Project>>>()
	val getProjectsFromRemote: LiveData<Result<List<Project>>> get() = _projects
	
	private val _streams = MutableLiveData<Result<List<StreamResponse>>>()
	val getStreamsFromRemote: LiveData<Result<List<StreamResponse>>> get() = _streams
	
	fun getStreamsFromLocal(): LiveData<List<Stream>> {
		return Transformations.map(streamDb.getAllResultsAsync().asLiveData()) { it }
	}
	
	fun getTrackingFromLocal(): LiveData<List<Tracking>> {
		return Transformations.map(trackingDb.getAllResultsAsync().asLiveData()) { it }
	}
	
	val nearbyGuardians = mutableListOf<EventGroup>()
	val othersGuardians = mutableListOf<EventGroup>()
	
	init {
		loadStreams()
	}
	
	fun getEventsCount(streamId: String): String = alertDb.getAlertCount(streamId).toString()
	
	fun fetchProjects() {
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
	
	fun loadStreams() {
		if (context.isNetworkAvailable()) {
			_streams.value = Result.Loading
			val preferences = Preferences.getInstance(context)
			val projectId = preferences.getInt(Preferences.SELECTED_PROJECT, -1)
			val project = projectDb.getProjectById(projectId)
			project?.serverId?.let { serverId ->
				getStreams.execute(object : DisposableSingleObserver<List<StreamResponse>>() {
					override fun onSuccess(t: List<StreamResponse>) {
						loadEvents(t)
						t.forEach { res ->
							streamDb.insertStream(res)
						}
						_streams.value = Result.Success(t)
					}
					
					override fun onError(e: Throwable) {
						_streams.value = Result.Error(e)
					}
				}, StreamsRequestFactory(projects = listOf(serverId)))
			}
		}
	}
	
	fun loadEvents(list: List<StreamResponse>) {
		list.forEach {
			getEvents.execute(object : DisposableSingleObserver<List<ResponseEvent>>() {
				override fun onSuccess(t: List<ResponseEvent>) {
					t.forEach { res ->
						alertDb.insertAlert(res)
					}
				}
				
				override fun onError(e: Throwable) {
					_streams.value = Result.Error(e)
				}
			}, it.id)
		}
	}
	
	fun getProjectsFromLocal(): List<Project> {
		return projectDb.getProjects()
	}
	
	fun getStreams(): List<Stream> = streamDb.getStreams()
	
	fun getProjectName(id: Int): String = getProject(id)?.name
			?: context.getString(R.string.all_projects)
	
	fun getProject(id: Int): Project? {
		return projectDb.getProjectById(id)
	}
	
	fun saveLastTimeToKnowTheCurrentLocation(context: Context, time: Long) {
		val preferences = Preferences.getInstance(context)
		preferences.putLong(Preferences.LATEST_CURRENT_LOCATION_TIME, time)
	}
	
	fun setProjectSelected(id: Int) {
		val preferences = Preferences.getInstance(context)
		preferences.putInt(Preferences.SELECTED_PROJECT, id)
	}
	
	fun handledStreams(lastLocation: Location?, streams: List<Stream>) {
		othersGuardians.clear()
		nearbyGuardians.clear()
		val groups = arrayListOf<EventGroup>()
		streams.forEach {
			var distance: Double? = null
			lastLocation?.let { loc ->
				distance = LatLng(it.latitude, it.longitude).distanceTo(LatLng(loc.latitude, loc.longitude))
			}
			groups.add(EventGroup(getEventsCount(it.serverId).toInt(), distance, it.name, it.serverId))
		}
		groups.sortBy { g -> g.distance }
		groups.forEach {
			if (it.distance == null) {
				othersGuardians.add(it)
			} else {
				if (it.distance >= 2000) {
					othersGuardians.add(it)
				} else {
					nearbyGuardians.add(it)
				}
			}
		}
		othersGuardians.sortByDescending { g -> g.eventSize }
	}
	
	fun handledStreamsResponse(lastLocation: Location?, list: List<StreamResponse>) {
		othersGuardians.clear()
		nearbyGuardians.clear()
		
		val groups = arrayListOf<EventGroup>()
		list.forEach {
			var distance: Double? = null
			lastLocation?.let { loc ->
				distance = LatLng(it.latitude, it.longitude).distanceTo(LatLng(loc.latitude, loc.longitude))
			}
			groups.add(EventGroup(it.eventsCount, distance, it.name, it.id))
		}
		groups.sortBy { g -> g.distance }
		groups.forEach {
			if (it.distance == null) {
				othersGuardians.add(it)
			} else {
				if (it.distance >= 2000) {
					othersGuardians.add(it)
				} else {
					nearbyGuardians.add(it)
				}
			}
		}
		othersGuardians.sortByDescending { g -> g.eventSize }
	}
	
	fun distance(lastLocation: Location, loc: Location): String = LatLng(loc.latitude, loc.longitude).distanceTo(LatLng(lastLocation.latitude, lastLocation.longitude)).toString()
	
	
	fun pushNotification(project: Project, callback: (Boolean) -> Unit) {
		CloudMessaging.unsubscribe(context) {
			project.serverId?.let { it1 -> CloudMessaging.setProject(context, it1) }
			CloudMessaging.subscribeIfRequired(context) {
				callback(true)
			}
		}
	}
}
