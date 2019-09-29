package org.rfcx.ranger.view.alerts

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.domain.BaseDisposableSingle
import org.rfcx.ranger.data.remote.groupByGuardians.GroupByGuardiansUseCase
import org.rfcx.ranger.data.remote.groupByGuardians.eventInGuardian.GetEventInGuardian
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventResponse
import org.rfcx.ranger.entity.event.EventsGuardianRequestFactory
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.entity.guardian.GroupByGuardiansResponse
import org.rfcx.ranger.entity.guardian.Guardian
import org.rfcx.ranger.util.Preferences

class GroupAlertsViewModel(private val context: Context, private val eventDb: EventDb, private val groupByGuardiansUseCase: GroupByGuardiansUseCase, private val getEventInGuardian: GetEventInGuardian) : ViewModel() {
	
	private val _items = MutableLiveData<GroupByGuardiansResponse>()
	val items: LiveData<GroupByGuardiansResponse> get() = _items
	
	private val _groupGuardianAlert = MutableLiveData<Result<List<GroupGuardianAlert>>>()
	val groupGuardianAlert: LiveData<Result<List<GroupGuardianAlert>>> get() = _groupGuardianAlert
	
	init {
		loadGuardianGroups()
	}
	
	private fun loadGuardianGroups() {
		val preferenceHelper = Preferences.getInstance(context)
		val shortName = preferenceHelper.getString(Preferences.SELECTED_GUARDIAN_GROUP)
		_groupGuardianAlert.value = Result.Loading
		groupByGuardiansUseCase.execute(object : DisposableSingleObserver<GroupByGuardiansResponse>(){
			override fun onSuccess(t: GroupByGuardiansResponse) {
				getEvents(t.guardians)
			}
			
			override fun onError(e: Throwable) {
				// TODO error @tree
			}
			
		}, shortName.toString())
	}
	
	fun getEvents(list: List<Guardian>) {
		val groupGuid = ArrayList<String>()
		val groupShortname = ArrayList<String>()
		
		// list = [{guid, name}, {guid, name}]
		list.forEach { guardian ->
			groupGuid.add(guardian.guid)
			groupShortname.add(guardian.name)
		}
		
		val requestFactory = EventsGuardianRequestFactory(groupGuid, "begins_at", "DESC", LIMITS, 0)
		getEventInGuardian.execute(object : DisposableSingleObserver<EventResponse>() {
			override fun onSuccess(t: EventResponse) {
				val groupGuardian = t.events?.let { groupGuardian(it) }
				if (groupGuardian != null) {
					handleOnSuccess(groupGuardian, groupShortname)
				}
			}
			
			override fun onError(e: Throwable) {
				// TODO onError @tree
			}
			
		}, requestFactory)
	}
	
	private fun handleOnSuccess(listGroupAlert: List<GroupAlert>, groupGuardian: ArrayList<String>){
		val group = ArrayList<GroupGuardianAlert>()

		groupGuardian.forEach { guardianShortname ->
			for(i in listGroupAlert.indices){
				if (guardianShortname == listGroupAlert[i].events[0].guardianShortname){
					val eventList = ArrayList<Event>()
					listGroupAlert[i].events.forEach {
						eventList.add(it)
					}
					val numEvents = listGroupAlert[i].events.size - numEvents(listGroupAlert[i].events)
					val groupGuardianAlert = GroupGuardianAlert(eventList, numEvents, guardianShortname)
					group.add(groupGuardianAlert)
				}else{
					val groupGuardianAlert = GroupGuardianAlert(null, null, guardianShortname)
					group.add(groupGuardianAlert)
				}
			}
		}
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
			if(state == ReviewEventFactory.confirmEvent || state == ReviewEventFactory.rejectEvent){
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