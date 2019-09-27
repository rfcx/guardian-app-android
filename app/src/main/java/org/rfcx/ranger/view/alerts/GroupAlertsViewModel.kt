package org.rfcx.ranger.view.alerts

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.domain.BaseDisposableSingle
import org.rfcx.ranger.data.remote.groupByGuardians.GroupByGuardiansUseCase
import org.rfcx.ranger.data.remote.groupByGuardians.eventInGuardian.GetEventInGuardian
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventResponse
import org.rfcx.ranger.entity.event.EventsGuardianRequestFactory
import org.rfcx.ranger.entity.guardian.GroupByGuardiansResponse
import org.rfcx.ranger.entity.guardian.Guardian
import org.rfcx.ranger.util.Preferences
import java.util.*
import kotlin.collections.ArrayList

class GroupAlertsViewModel(private val context: Context, private val groupByGuardiansUseCase: GroupByGuardiansUseCase, private val getEventInGuardian: GetEventInGuardian) : ViewModel() {
	
	private val _items = MutableLiveData<Result<GroupByGuardiansResponse>>()
	val items: LiveData<Result<GroupByGuardiansResponse>> get() = _items
	
	private val _groupsEventsGuardian = MutableLiveData<Result<ArrayList<MutableList<Event>>>>()
	val groupsEventsGuardian: LiveData<Result<ArrayList<MutableList<Event>>>> get() = _groupsEventsGuardian
	
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
				t.events?.let { makeGroupGuardian(it) }
			}
			
			override fun onError(e: Throwable) {
				// TODO onError @tree
			}
			
		}, requestFactory)
	}
	
	private fun makeGroupGuardian(list: List<Event>){
		val name = Array(list.size){ i -> list[i].guardianShortname.toString()}
		val listShortName = removeArrayDuplicates(name)
		
		val listEvents = ArrayList<Event>()
		val allListEvents: ArrayList<MutableList<Event>> = ArrayList()
		
		listShortName.forEach { shortName ->
			list.forEach { event ->
				if(shortName == event.guardianShortname){
					listEvents.add(event)
				}
			}
			allListEvents.addAll(listOf(listEvents))
			listEvents.clear()
		}
		Log.d("AllListEvents", allListEvents.size.toString())
	}
	
	private fun removeArrayDuplicates(duplicates: Array<String>): Array<String> {
		return Arrays.asList(*duplicates).toSet().toTypedArray()
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
