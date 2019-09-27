package org.rfcx.ranger.view.alerts

import android.content.Context
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
	
	private val _items = MutableLiveData<Result<GroupByGuardiansResponse>>()
	val items: LiveData<Result<GroupByGuardiansResponse>> get() = _items
	
	init {
		loadGuardianGroups()
	}
	
	private fun loadGuardianGroups() {
		val preferenceHelper = Preferences.getInstance(context)
		val shortName = preferenceHelper.getString(Preferences.SELECTED_GUARDIAN_GROUP)
		_items.value = Result.Loading
		groupByGuardiansUseCase.execute(GetGroupByGuardianDisposable(_items), shortName.toString())
	}
	
	fun getEvents(list: List<Guardian>) {
		val groupGuid = ArrayList<String>()
		list.forEach { guardian ->
			groupGuid.add(guardian.guid)
		}
		
		val requestFactory = EventsGuardianRequestFactory(groupGuid, "begins_at", "DESC", LIMITS, 0)
		getEventInGuardian.execute(object : DisposableSingleObserver<EventResponse>() {
			override fun onSuccess(t: EventResponse) {
				val groupGuardian = t.events?.let { groupGuardian(it) }
				if (groupGuardian != null) {
					countEvents(groupGuardian)
				}
			}
			
			override fun onError(e: Throwable) {
				// TODO onError @tree
			}
			
		}, requestFactory)
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
	
	private fun countEvents(groupAlert: List<GroupAlert>) {
		val countList = ArrayList<Int>()
		var count = 0
		
		groupAlert.forEach { listEvent ->
			listEvent.events.forEach { event ->
				val state = eventDb.getEventState(event.event_guid)
				state?.let {
					when (it) {
						ReviewEventFactory.confirmEvent -> count += 1
						ReviewEventFactory.rejectEvent -> count += 1
					}
				}
			}
			countList.add(count)
			count = 0
		}
	}
	
	companion object {
		const val LIMITS = 50
	}
}

class GetGroupByGuardianDisposable(
		private val liveData: MutableLiveData<Result<GroupByGuardiansResponse>>)
	: BaseDisposableSingle<GroupByGuardiansResponse>() {
	override fun onSuccess(success: Result<GroupByGuardiansResponse>) {
		liveData.value = success
	}
	
	override fun onError(e: Throwable, error: Result<GroupByGuardiansResponse>) {
		liveData.value = error
	}
}

data class GroupAlert(val events: List<Event>)