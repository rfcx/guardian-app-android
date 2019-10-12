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
	
	private val _items = MutableLiveData<GroupByGuardiansResponse>()
	val items: LiveData<GroupByGuardiansResponse> get() = _items
	
	private val _groupGuardianAlert = MutableLiveData<Result<List<GroupGuardianAlert>>>()
	val groupGuardianAlert: LiveData<Result<List<GroupGuardianAlert>>> get() = _groupGuardianAlert
	
	private var _alertsList: List<GroupGuardianAlert> = listOf()
	
	fun loadGuardianGroups() {
		_groupGuardianAlert.value = Result.Loading
		getEventsCache()
		
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
				_groupGuardianAlert.value = e.getResultError()
			}
			
		}, group)
	}
	
	private fun getEvents(list: List<Guardian>) {
		val groupGuid = ArrayList<String>()
		val groupShortname = ArrayList<String>()
		
		list.forEach { guardian ->
			groupGuid.add(guardian.guid)
			groupShortname.add(guardian.name)
		}
		
		val group = context.getGuardianGroup()
		if (group == null) {
			Toast.makeText(context, context.getString(R.string.error_no_guardian_group_set), Toast.LENGTH_SHORT).show()
			return
		}
		val guardianGroup = ArrayList<String>()
		guardianGroup.add(group)
		val requestFactory = EventsRequestFactory(guardianGroup, "begins_at", "DESC", LIMITS, 0)
		eventsUserCase.execute(object : DisposableSingleObserver<EventResponse>() {
			override fun onSuccess(t: EventResponse) {
				val groupGuardian = t.events?.let { groupGuardian(it) }
				if (groupGuardian != null) {
					handleOnSuccess(groupGuardian, groupShortname)
				}
			}
			
			override fun onError(e: Throwable) {
				_groupGuardianAlert.value = e.getResultError()
			}
		}, requestFactory)
	}
	
	private fun handleOnSuccess(listGroupAlert: List<GroupAlert>, groupGuardian: ArrayList<String>) {
		val group = ArrayList<GroupGuardianAlert>()
		
		groupGuardian.forEach { guardianShortname ->
			val filters = listGroupAlert.filter { it.events[0].guardianShortname == guardianShortname }
			if (filters.isNotEmpty()) {
				val eventList = ArrayList<Event>()
				filters[0].events.forEach { eventList.add(it) }
				val numEvents = filters[0].events.size - numEvents(filters[0].events)
				val groupGuardianAlert = GroupGuardianAlert(eventList, numEvents, guardianShortname)
				group.add(groupGuardianAlert)
			} else {
				val groupGuardianAlert = GroupGuardianAlert(null, null, guardianShortname)
				group.add(groupGuardianAlert)
			}
		}
		_alertsList = group
		_groupGuardianAlert.value = Result.Success(group)
	}
	
	private fun getEventsCache() {
		val cacheEvents = eventDb.getEvents()
		val groupGuardian = groupGuardian(cacheEvents)
		
		val mainGroups = arrayListOf<String>()
		cacheEvents.distinctBy { it.guardianShortname }.mapTo(mainGroups, { it.guardianShortname!! })
		
		val group = ArrayList<GroupGuardianAlert>()
		
		mainGroups.forEach { guardianShortname ->
			val filters = groupGuardian.filter { it.events[0].guardianShortname == guardianShortname }
			if (filters.isNotEmpty()) {
				val eventList = ArrayList<Event>()
				filters[0].events.forEach { eventList.add(it) }
				val numEvents = filters[0].events.size - numEvents(filters[0].events)
				val groupGuardianAlert = GroupGuardianAlert(eventList, numEvents, guardianShortname)
				group.add(groupGuardianAlert)
			} else {
				val groupGuardianAlert = GroupGuardianAlert(null, null, guardianShortname)
				group.add(groupGuardianAlert)
			}
		}
		_alertsList = group
		_groupGuardianAlert.value = Result.Success(group)
	}
	
	fun updateNumberUnreview() {
		val group = ArrayList<GroupGuardianAlert>()
		_alertsList.forEach { groupGuardianAlert ->
			if (groupGuardianAlert.events !== null) {
				val numEvents = groupGuardianAlert.events.size - numEvents(groupGuardianAlert.events)
				val groupGuardianAlert = GroupGuardianAlert(groupGuardianAlert.events, numEvents, groupGuardianAlert.name)
				group.add(groupGuardianAlert)
			} else {
				val groupGuardianAlert = GroupGuardianAlert(null, null, groupGuardianAlert.name)
				group.add(groupGuardianAlert)
			}
		}
		_alertsList = group
		_groupGuardianAlert.value = Result.Success(group)
	}
	
	private fun groupGuardian(events: List<Event>): List<GroupAlert> {
		// find main group
		val mainGroups = arrayListOf<String>()
		events.distinctBy { it.guardianGUID }.mapTo(mainGroups, { it.guardianGUID!! })
		// split group
		val groupAlerts = arrayListOf<GroupAlert>()
		mainGroups.forEach { guid ->
			val eventList = arrayListOf<Event>()
			
			events.forEach { event ->
				if (event.guardianGUID == guid) {
					eventList.add(event)
				}
			}
			groupAlerts.add(GroupAlert(eventList))
		}
		return groupAlerts
	}
	
	private fun numEvents(groupAlert: List<Event>): Int {
		var count = 0
		groupAlert.forEach { event ->
			
			val state = eventDb.getEventState(event.event_guid)
			if (state == ReviewEventFactory.confirmEvent || state == ReviewEventFactory.rejectEvent) {
				count += 1
			}
		}
		return count
	}
	
	companion object {
		const val LIMITS = 50
	}
}

data class GroupAlert(val events: List<Event>)

data class GroupGuardianAlert(val events: ArrayList<Event>?, val unread: Int?, val name: String)