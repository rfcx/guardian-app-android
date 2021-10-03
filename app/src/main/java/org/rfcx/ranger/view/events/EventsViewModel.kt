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
import org.rfcx.ranger.data.api.project.GetProjectsUseCase
import org.rfcx.ranger.data.api.project.ProjectResponse
import org.rfcx.ranger.data.api.project.ProjectsRequestFactory
import org.rfcx.ranger.data.api.site.GetStreamsUseCase
import org.rfcx.ranger.data.api.site.StreamResponse
import org.rfcx.ranger.data.api.site.StreamsRequestFactory
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.local.ProfileData
import org.rfcx.ranger.data.local.ProjectDb
import org.rfcx.ranger.data.remote.ResponseCallback
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.domain.alert.GetEventsUseCase
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventsRequestFactory
import org.rfcx.ranger.entity.project.Project
import org.rfcx.ranger.util.Preferences
import org.rfcx.ranger.util.asLiveData
import org.rfcx.ranger.view.events.adapter.EventGroup


class EventsViewModel(private val context: Context, private val profileData: ProfileData, private val getProjects: GetProjectsUseCase, private val projectDb: ProjectDb, private val eventDb: EventDb, private val eventsUserCase: GetEventsUseCase, private val getStreams: GetStreamsUseCase) : ViewModel() {
	private val _projects = MutableLiveData<Result<List<Project>>>()
	val projects: LiveData<Result<List<Project>>> get() = _projects
	
	private val _streams = MutableLiveData<Result<List<StreamResponse>>>()
	val streams: LiveData<Result<List<StreamResponse>>> get() = _streams
	
	fun getAlerts(): LiveData<List<Event>> {
		return Transformations.map(eventDb.getAllResultsAsync().asLiveData()) { it }
	}
	
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
					_streams.value = Result.Success(t)
				}
				
				override fun onError(e: Throwable) {
					_streams.value = Result.Error(e)
				}
			}, StreamsRequestFactory(projects = listOf(serverId)))
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
			val distance = LatLng(it.latitude, it.longitude).distanceTo(LatLng(lastLocation.latitude, lastLocation.longitude))
			groups.add(EventGroup(listOf(), distance, it.name))
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
	
	fun handledGuardians(lastLocation: Location) {
		val events = eventDb.getEvents()
		othersGuardians.clear()
		nearbyGuardians.clear()
		
		// find main Guardian
		val mainGroups = arrayListOf<String>()
		events.distinctBy { it.guardianId }.mapTo(mainGroups, { it.guardianId })
		
		// split group
		val groups = arrayListOf<EventGroup>()
		mainGroups.forEach { guid ->
			val eventsOfGuardian = events.filter { it.guardianId == guid }
			val shortName = eventsOfGuardian.first { it.guardianId == guid }.guardianName
			var distance = 0.0
			if (eventsOfGuardian.isNotEmpty()) {
				distance = LatLng(eventsOfGuardian[0].latitude ?: 0.0, eventsOfGuardian[0].longitude
						?: 0.0).distanceTo(LatLng(lastLocation.latitude, lastLocation.longitude))
			}
			groups.add(EventGroup(eventsOfGuardian, distance, shortName))
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
