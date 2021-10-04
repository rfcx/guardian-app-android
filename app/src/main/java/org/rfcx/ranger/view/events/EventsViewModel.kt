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
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.data.local.ProjectDb
import org.rfcx.ranger.data.remote.ResponseCallback
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.domain.alert.GetEventsUseCase
import org.rfcx.ranger.entity.Stream
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventsRequestFactory
import org.rfcx.ranger.entity.project.Project
import org.rfcx.ranger.localdb.StreamDb
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.asLiveData
import org.rfcx.ranger.view.events.adapter.EventGroup


class EventsViewModel(private val context: Context, private val profileData: ProfileData, private val getProjects: GetProjectsUseCase, private val projectDb: ProjectDb, private val streamDb: StreamDb, private val alertDb: AlertDb, private val eventDb: EventDb, private val eventsUserCase: GetEventsUseCase, private val getStreams: GetStreamsUseCase, private val getEvents: GetEvents) : ViewModel() {
	private val _projects = MutableLiveData<Result<List<Project>>>()
	val projects: LiveData<Result<List<Project>>> get() = _projects
	
	private val _streams = MutableLiveData<Result<List<StreamResponse>>>()
	val streams: LiveData<Result<List<StreamResponse>>> get() = _streams
	
	fun getAlerts(): LiveData<List<Event>> {
		return Transformations.map(eventDb.getAllResultsAsync().asLiveData()) { it }
	}
	
	fun getStreamsFromLocal(): LiveData<List<Stream>> {
		return Transformations.map(streamDb.getAllResultsAsync().asLiveData()) { it }
	}
	
	var listEvent: ArrayList<List<ResponseEvent>> = arrayListOf()
	
	val nearbyGuardians = mutableListOf<EventGroup>()
	val othersGuardians = mutableListOf<EventGroup>()
	
	init {
		loadAlerts()
		loadStreams()
	}
	
	fun fetchProjects() {
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
	
	fun loadStreams() {
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
				}
				
				override fun onError(e: Throwable) {
					_streams.value = Result.Error(e)
				}
			}, StreamsRequestFactory(projects = listOf(serverId)))
		}
	}
	
	fun loadEvents(list: List<StreamResponse>) {
		listEvent = arrayListOf()
		list.forEach {
			getEvents.execute(object : DisposableSingleObserver<List<ResponseEvent>>() {
				override fun onSuccess(t: List<ResponseEvent>) {
					listEvent.add(t)
					_streams.value = Result.Success(list)
					t.forEach { res ->
						alertDb.insertAlert(res)
					}
				}
				
				override fun onError(e: Throwable) {}
			}, it.id)
		}
	}
	
	fun getProjectsFromLocal(): List<Project> {
		return projectDb.getProjects()
	}
	
	fun getProjectName(): String {
		val preferences = Preferences.getInstance(context)
		val projectId = preferences.getInt(Preferences.SELECTED_PROJECT, -1)
		val project = projectDb.getProjectById(projectId)
		return project?.name ?: context.getString(R.string.all_projects)
	}
	
	fun setProjectSelected(id: Int) {
		val preferences = Preferences.getInstance(context)
		preferences.putInt(Preferences.SELECTED_PROJECT, id)
	}
	
	fun handledStreams(lastLocation: Location, list: List<StreamResponse>) {
		othersGuardians.clear()
		nearbyGuardians.clear()
		
		val groups = arrayListOf<EventGroup>()
		list.forEach {
			val events = listEvent.filter { list -> list.any { e -> e.streamId == it.id } }
			val distance = LatLng(it.latitude, it.longitude).distanceTo(LatLng(lastLocation.latitude, lastLocation.longitude))
			groups.add(EventGroup(if (events.isEmpty()) listOf() else events[0], distance, it.name, it.id))
		}
		groups.sortBy { g -> g.distance }
		groups.forEach {
			if (it.distance >= 2000) {
				othersGuardians.add(it)
			} else {
				nearbyGuardians.add(it)
			}
		}
		othersGuardians.sortByDescending { g -> g.events.size }
	}
	
	private fun loadAlerts() {
		val group = profileData.getGuardianGroup() ?: return
		val requestFactory = EventsRequestFactory(listOf(group.shortname), "measured_at", "DESC", LIMIT_EVENTS, 0, group.values)
		
		eventsUserCase.execute(object : ResponseCallback<Pair<List<Event>, Int>> {
			override fun onSuccess(t: Pair<List<Event>, Int>) {}
			
			override fun onError(e: Throwable) {}
		}, requestFactory)
	}
	
	companion object {
		const val LIMIT_EVENTS = 100
	}
}
