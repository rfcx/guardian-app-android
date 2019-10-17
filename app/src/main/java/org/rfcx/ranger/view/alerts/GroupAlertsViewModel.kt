package org.rfcx.ranger.view.alerts

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.R
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.domain.alert.GetEventsUseCase
import org.rfcx.ranger.data.remote.groupByGuardians.GroupByGuardiansUseCase
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventResponse
import org.rfcx.ranger.entity.event.EventsRequestFactory
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.entity.guardian.GroupByGuardiansResponse
import org.rfcx.ranger.entity.guardian.Guardian
import org.rfcx.ranger.util.getGuardianGroup
import org.rfcx.ranger.util.getResultError

class GroupAlertsViewModel(private val context: Context, private val eventDb: EventDb, private val groupByGuardiansUseCase: GroupByGuardiansUseCase, private val eventsUserCase: GetEventsUseCase) : ViewModel() {
	
	private val _status = MutableLiveData<Result<List<EventGroup>>>()
	val status: LiveData<Result<List<EventGroup>>> get() = _status
	
	private var _eventGroups: List<EventGroup> = listOf()
	
	fun loadGuardianGroups() {
		_status.value = Result.Loading
		
		updateEvents(eventDb.getEvents(), listOf())
		
		val group = context.getGuardianGroup()
		if (group == null) {
			Toast.makeText(context, context.getString(R.string.error_no_guardian_group_set), Toast.LENGTH_SHORT).show()
			return
		}
		groupByGuardiansUseCase.execute(object : DisposableSingleObserver<GroupByGuardiansResponse>() {
			override fun onSuccess(t: GroupByGuardiansResponse) {
				getEvents(t.guardians)
			}
			
			override fun onError(e: Throwable) {
				_status.value = e.getResultError()
			}
			
		}, group)
	}
	
	private fun getEvents(guardians: List<Guardian>) {
		val group = context.getGuardianGroup()
		if (group == null) {
			Toast.makeText(context, context.getString(R.string.error_no_guardian_group_set), Toast.LENGTH_SHORT).show()
			return
		}
		
		val requestFactory = EventsRequestFactory(group, "begins_at", "DESC", LIMITS, 0)
		eventsUserCase.execute(object : DisposableSingleObserver<EventResponse>() {
			override fun onSuccess(t: EventResponse) {
				t.events?.let { events ->
					updateEvents(events, guardians, true)
				}
			}
			
			override fun onError(e: Throwable) {
				_status.value = e.getResultError()
			}
		}, requestFactory)
	}
	
	private fun updateEvents(events: List<Event>, guardians: List<Guardian>, complete: Boolean = false) {
		val guardianGuidsWithEvents = events.map { it.guardianGUID }.toSet()
		val guardiansWithoutEvents = guardians.filter { !guardianGuidsWithEvents.contains(it.guid) }.map { EventGroup(listOf(), it.guid, it.name) }
		val guardiansWithEvents = groupGuardian(events)
		
		_eventGroups = guardiansWithEvents + guardiansWithoutEvents
		
		if (complete || events.size > 0) {
			_status.value = Result.Success(_eventGroups)
		}
	}
	
	private fun groupGuardian(events: List<Event>): List<EventGroup> {
		// find main group
		val mainGroups = arrayListOf<String>()
		events.distinctBy { it.guardianGUID }.mapTo(mainGroups, { it.guardianGUID!! })
		// split group
		val groupAlerts = arrayListOf<EventGroup>()
		mainGroups.forEach { guid ->
			val shortname = events.filter { it.guardianGUID == guid }.first().guardianShortname ?: ""
			val eventList = arrayListOf<Event>()
			
			events.forEach { event ->
				if (event.guardianGUID == guid) {
					eventList.add(event)
				}
			}
			groupAlerts.add(EventGroup(eventList, guid, shortname))
		}
		return groupAlerts
	}

	
	companion object {
		const val LIMITS = 50
	}
}

data class EventGroup(val events: List<Event>, val guardianGuid: String, val guardianName: String) {
	
	fun numberOfUnread(eventDb: EventDb): Int {
		val read = events.fold(0) { acc, event ->
			val state = eventDb.getEventState(event.event_guid)
			if (state == ReviewEventFactory.confirmEvent || state == ReviewEventFactory.rejectEvent) acc + 1 else acc
		}
		return events.size - read
	}
}

