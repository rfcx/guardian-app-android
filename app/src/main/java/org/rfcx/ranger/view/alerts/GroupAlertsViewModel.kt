package org.rfcx.ranger.view.alerts

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
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
	
	private val _status = MutableLiveData<Result<List<EventGroup>>>()
	val status: LiveData<Result<List<EventGroup>>> get() = _status
	
	private lateinit var eventLiveData: LiveData<List<Event>>
	private var _eventGroups: List<EventGroup> = listOf()
	
	private val eventObserve = Observer<List<Event>> {
		if (it.isNotEmpty()) {
			val events = eventDb.getEvents()
			loadGuardianGroups(events)
		}
	}
	
	init {
		_status.value = Result.Loading
		fetchEvents()
	}
	
	private fun fetchEvents() {
		eventLiveData = Transformations.map<RealmResults<Event>,
				List<Event>>(eventDb.getAllResultsAsync().asLiveData()) {
			it
		}
		eventLiveData.observeForever(eventObserve)
	}
	
	private fun loadGuardianGroups(events: List<Event>) {
		val group = context.getGuardianGroup()
		if (group == null) {
			Toast.makeText(context, context.getString(R.string.error_no_guardian_group_set), Toast.LENGTH_SHORT).show()
			return
		}
		groupByGuardiansUseCase.execute(object : ResponseCallback<List<Guardian>> {
			override fun onSuccess(t: List<Guardian>) {
				updateEvents(events, t)
			}
			
			override fun onError(e: Throwable) {
				_status.value = e.getResultError()
			}
		}, group)
	}
	
	private fun updateEvents(events: List<Event>, guardians: List<Guardian>) {
		val guardianGuidsWithEvents = events.map { it.guardianId }.toSet()
		val guardiansWithoutEvents = guardians.filter { !guardianGuidsWithEvents.contains(it.guid) }.map { EventGroup(listOf(), it.guid, it.name) }
		val guardiansWithEvents = groupGuardian(events)
		
		_eventGroups = guardiansWithEvents + guardiansWithoutEvents
		
		if (events.isNotEmpty()) {
			_status.value = Result.Success(_eventGroups)
		}
	}
	
	private fun groupGuardian(events: List<Event>): List<EventGroup> {
		// find main group
		val mainGroups = arrayListOf<String>()
		events.distinctBy { it.guardianId }.mapTo(mainGroups, { it.guardianId })
		// split group
		val groupAlerts = arrayListOf<EventGroup>()
		mainGroups.forEach { guid ->
			val shortname = events.filter { it.guardianId == guid }.first().guardianName
			val eventList = arrayListOf<Event>()
			
			events.forEach { event ->
				if (event.guardianId == guid) {
					eventList.add(event)
				}
			}
			groupAlerts.add(EventGroup(eventList, guid, shortname))
		}
		return groupAlerts
	}
}

data class EventGroup(val events: List<Event>, val guardianGuid: String, val guardianName: String) {
	
	fun numberOfUnread(eventDb: EventDb): Int {
		val read = events.fold(0) { acc, event ->
			val state = eventDb.getEventState(event.id)
			if (state == ReviewEventFactory.confirmEvent || state == ReviewEventFactory.rejectEvent) acc + 1 else acc
		}
		return events.size - read
	}
}

