package org.rfcx.ranger.view.alerts

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.*
import io.realm.RealmResults
import org.rfcx.ranger.R
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.remote.ResponseCallback
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.groupByGuardians.GroupByGuardiansUseCase
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.entity.guardian.Guardian
import org.rfcx.ranger.util.asLiveData
import org.rfcx.ranger.util.getGuardianGroup
import org.rfcx.ranger.util.getResultError

class GroupAlertsViewModel(private val context: Context, private val eventDb: EventDb,
                           private val groupByGuardiansUseCase: GroupByGuardiansUseCase) : ViewModel() {
	
	private val _groups = MutableLiveData<Result<List<EventGroup>>>()
	val groups: LiveData<Result<List<EventGroup>>> get() = _groups
	
	private lateinit var eventLiveData: LiveData<List<Event>>
	private var _eventGroups: List<EventGroup> = listOf()
	private var _events: List<Event> = listOf()
	var isRefreshing = false
	
	private val eventObserve = Observer<List<Event>> {
		if (it.isNotEmpty()) {
			_events = eventDb.getEvents()
			loadGuardianGroups(events = _events)
		}
	}
	
	init {
		fetchEvents()
	}
	
	fun refresh() {
		isRefreshing = true
		loadGuardianGroups(true, _events)
	}
	
	private fun fetchEvents() {
		_groups.value = Result.Loading
		eventLiveData = Transformations.map<RealmResults<Event>,
				List<Event>>(eventDb.getAllResultsAsync().asLiveData()) {
			it
		}
		eventLiveData.observeForever(eventObserve)
	}
	
	private fun loadGuardianGroups(force: Boolean = false, events: List<Event>) {
		val group = context.getGuardianGroup() ?: return
		groupByGuardiansUseCase.execute(object : ResponseCallback<List<Guardian>> {
			override fun onSuccess(t: List<Guardian>) {
				updateEvents(events, t)
			}
			
			override fun onError(e: Throwable) {
				_groups.value = e.getResultError()
			}
		}, group, force)
	}
	
	private fun updateEvents(events: List<Event>, guardians: List<Guardian>) {
		val guardianGuidsWithEvents = events.map { it.guardianId }.toSet()
		val guardiansWithoutEvents = guardians.filter {
			!guardianGuidsWithEvents.contains(it.guid)
		}.map { EventGroup(0, it.guid, it.name, 0) }
		val guardiansWithEvents = groupGuardian(events)
		_eventGroups = guardiansWithEvents + guardiansWithoutEvents
		isRefreshing = false
		_groups.value = Result.Success(_eventGroups)
	}
	
	private fun groupGuardian(events: List<Event>): List<EventGroup> {
		// find main group
		val mainGroups = arrayListOf<String>()
		events.distinctBy { it.guardianId }.mapTo(mainGroups, { it.guardianId })
		// split group
		val groupAlerts = arrayListOf<EventGroup>()
		mainGroups.forEach { guid ->
			val eventsOfGuardian = events.filter { it.guardianId == guid }
			val shortName = eventsOfGuardian.first { it.guardianId == guid }.guardianName
			var unreadCount = 0
			if (eventsOfGuardian.isNotEmpty()) {
				val read = eventsOfGuardian.fold(0) { acc, event ->
					val state = eventDb.getEventState(event.id)
					if (state == ReviewEventFactory.confirmEvent ||
							state == ReviewEventFactory.rejectEvent) acc + 1 else acc
				}
				unreadCount = eventsOfGuardian.size - read
			}
			groupAlerts.add(EventGroup(eventsOfGuardian.size, guid, shortName, unreadCount))
		}
		return groupAlerts
	}
}

data class EventGroup(val events: Int, val guardianGuid: String, val guardianName: String,
                      val unReadCount: Int)
