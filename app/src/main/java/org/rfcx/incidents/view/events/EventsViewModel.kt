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
import org.rfcx.incidents.data.api.events.GetEvents
import org.rfcx.incidents.data.api.events.ResponseEvent
import org.rfcx.incidents.data.api.incident.IncidentRequestFactory
import org.rfcx.incidents.data.api.incident.IncidentUseCase
import org.rfcx.incidents.data.api.incident.IncidentsResponse
import org.rfcx.incidents.data.api.project.GetProjectsUseCase
import org.rfcx.incidents.data.api.project.ProjectResponse
import org.rfcx.incidents.data.api.project.ProjectsRequestFactory
import org.rfcx.incidents.data.api.site.GetStreamsUseCase
import org.rfcx.incidents.data.api.site.StreamResponse
import org.rfcx.incidents.data.api.site.StreamsRequestFactory
import org.rfcx.incidents.data.api.site.toStream
import org.rfcx.incidents.data.local.AlertDb
import org.rfcx.incidents.data.local.ProjectDb
import org.rfcx.incidents.data.remote.Result
import org.rfcx.incidents.entity.Stream
import org.rfcx.incidents.entity.alert.Alert
import org.rfcx.incidents.entity.location.Tracking
import org.rfcx.incidents.entity.project.Project
import org.rfcx.incidents.localdb.StreamDb
import org.rfcx.incidents.localdb.TrackingDb
import org.rfcx.incidents.util.Preferences
import org.rfcx.incidents.util.asLiveData
import org.rfcx.incidents.util.dateRangeFormat
import org.rfcx.incidents.util.isNetworkAvailable
import org.rfcx.incidents.view.events.adapter.StreamItem


class EventsViewModel(private val context: Context, private val getProjects: GetProjectsUseCase, private val getIncidents: IncidentUseCase, private val projectDb: ProjectDb, private val streamDb: StreamDb, private val trackingDb: TrackingDb, private val alertDb: AlertDb, private val getStreams: GetStreamsUseCase, private val getEvents: GetEvents) : ViewModel() {
	
	val streamItems = mutableListOf<StreamItem>()
	
	private val _projects = MutableLiveData<Result<List<Project>>>()
	val getProjectsFromRemote: LiveData<Result<List<Project>>> get() = _projects
	
	private val _streams = MutableLiveData<Result<List<StreamResponse>>>()
	val getStreamsFromRemote: LiveData<Result<List<StreamResponse>>> get() = _streams
	
	private val _alerts = MutableLiveData<Result<List<ResponseEvent>>>()
	val getAlertsFromRemote: LiveData<Result<List<ResponseEvent>>> get() = _alerts
	
	private val _incidents = MutableLiveData<Result<List<IncidentsResponse>>>()
	val getIncidentsFromRemote: LiveData<Result<List<IncidentsResponse>>> get() = _incidents
	
	fun getStreamsFromLocal(): LiveData<List<Stream>> {
		return Transformations.map(streamDb.getAllResultsAsync().asLiveData()) { it }
	}
	
	fun getAlertsFromLocal(): LiveData<List<Alert>> {
		return Transformations.map(alertDb.getAllResultsAsync().asLiveData()) { it }
	}
	
	fun getTrackingFromLocal(): LiveData<List<Tracking>> {
		return Transformations.map(trackingDb.getAllResultsAsync().asLiveData()) { it }
	}
	
	fun getEventsCount(streamId: String): String = alertDb.getAlertCount(streamId).toString()
	
	private fun getAlerts(streamId: String): List<Alert> = alertDb.getAlerts(streamId)
	
	private fun fetchEvents(streamId: String) {
		_alerts.value = Result.Loading
		
		getEvents.execute(object : DisposableSingleObserver<List<ResponseEvent>>() {
			override fun onSuccess(t: List<ResponseEvent>) {
				if (alertDb.getAlertCount(streamId).toInt() != t.size) {
					alertDb.deleteAlertsByStreamId(streamId)
					t.forEach { res ->
						alertDb.insertAlert(res)
					}
				}
				_alerts.value = Result.Success(t)
			}
			
			override fun onError(e: Throwable) {
				_alerts.value = Result.Error(e)
			}
		}, streamId)
	}
	
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
	
	fun getIncidents(stream: Stream) {
		if (context.isNetworkAvailable()) {
			getIncidents.execute(object : DisposableSingleObserver<List<IncidentsResponse>>() {
				override fun onSuccess(t: List<IncidentsResponse>) {
					t.map { item ->
						val streamAddIncidentRef = Stream(stream.id, stream.serverId, stream.name, stream.latitude, stream.longitude, stream.projectServerId, item.incidents.items[0].ref)
						streamDb.saveIncidentRef(streamAddIncidentRef)
					}
					_incidents.value = Result.Success(t)
				}
				
				override fun onError(e: Throwable) {
					_incidents.value = Result.Error(e)
				}
			}, IncidentRequestFactory(keyword = stream.name))
		}
	}
	
	fun loadStreams() {
		if (context.isNetworkAvailable()) {
			val preferences = Preferences.getInstance(context)
			val projectId = preferences.getInt(Preferences.SELECTED_PROJECT, -1)
			val project = projectDb.getProjectById(projectId)
			project?.serverId?.let { serverId ->
				getStreams.execute(object : DisposableSingleObserver<List<StreamResponse>>() {
					override fun onSuccess(t: List<StreamResponse>) {
						loadEvents(t)
						t.forEach { res ->
							streamDb.insertStream(res)
							getIncidents(res.toStream())
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
			fetchEvents(it.id)
		}
	}
	
	fun getProjectsFromLocal(): List<Project> {
		return projectDb.getProjects()
	}
	
	fun getStreams(): List<Stream> = streamDb.getStreams()
	
	fun isStreamsEmpty(projectServerId: String): Boolean = streamDb.getStreams().none { s -> s.projectServerId == projectServerId }
	
	fun getProjectName(id: Int): String = getProject(id)?.name
			?: context.getString(R.string.all_projects)
	
	fun getProject(id: Int): Project? {
		return projectDb.getProjectById(id)
	}
	
	private fun getDateTime(streamServerId: String): String? {
		val firstEvent = getFirstEvent(streamServerId)
		val lastEvent = getLastEvent(streamServerId)
		
		if (firstEvent == null || lastEvent == null) return null
		return dateRangeFormat(context, firstEvent.start, lastEvent.end)
	}
	
	private fun getFirstEvent(streamServerId: String): Alert? = getAlerts(streamServerId).minByOrNull { a -> a.start }
	private fun getLastEvent(streamServerId: String): Alert? = getAlerts(streamServerId).maxByOrNull { a -> a.start }
	
	fun saveLastTimeToKnowTheCurrentLocation(context: Context, time: Long) {
		val preferences = Preferences.getInstance(context)
		preferences.putLong(Preferences.LATEST_CURRENT_LOCATION_TIME, time)
	}
	
	fun setProjectSelected(id: Int) {
		val preferences = Preferences.getInstance(context)
		preferences.putInt(Preferences.SELECTED_PROJECT, id)
	}
	
	fun handledStreams(streams: List<Stream>) {
		streamItems.clear()
		streams.forEach {
			streamItems.add(StreamItem(getEventsCount(it.serverId).toInt(), it.incidentRef, null, it.name, it.serverId, getDateTime(it.serverId), getAlerts(it.serverId)))
		}
		streamItems.sortByDescending { g -> g.eventSize }
	}
	
	fun distance(lastLocation: Location, loc: Location): String = LatLng(loc.latitude, loc.longitude).distanceTo(LatLng(lastLocation.latitude, lastLocation.longitude)).toString()
	
}
