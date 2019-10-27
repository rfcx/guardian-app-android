package org.rfcx.ranger.view.alerts.GuardianListDetail

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.observers.DisposableSingleObserver
import org.rfcx.ranger.R
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.groupByGuardians.eventInGuardian.GetMoreEventInGuardian
import org.rfcx.ranger.entity.event.*
import org.rfcx.ranger.util.getResultError
import org.rfcx.ranger.util.replace
import org.rfcx.ranger.view.alerts.adapter.EventItem
import java.util.*
import kotlin.collections.ArrayList

class GuardianListDetailViewModel(private val context: Context, private val eventDb: EventDb, private val getMoreEvent: GetMoreEventInGuardian) : ViewModel() {
	private val _items = MutableLiveData<Result<ArrayList<GuardianListDetail>>>()
	val items: LiveData<Result<ArrayList<GuardianListDetail>>> get() = _items
	
	lateinit var value: String
	
	private var eventOfAmazon: MutableList<Event> = mutableListOf()
	private var eventOfMacaw: MutableList<Event> = mutableListOf()
	private var eventOfChainsaw: MutableList<Event> = mutableListOf()
	private var eventOfVehicle: MutableList<Event> = mutableListOf()
	private var eventOfGunshot: MutableList<Event> = mutableListOf()
	private var eventOfTrespasser: MutableList<Event> = mutableListOf()
	private var eventOfOther: MutableList<Event> = mutableListOf()
	private var eventOfMismatch: MutableList<Event> = mutableListOf()
	var loading = MutableLiveData<StateLoading>()
	
	var eventAll: ArrayList<MutableList<Event>> = ArrayList()
	private var _alertsList: List<EventItem> = listOf()
	
	fun makeGroupOfValue(events: List<Event>) {
		_items.value = Result.Loading
		events.forEach { event ->
			when {
				event.value == "amazon" -> {
					eventOfAmazon.add(event)
				}
				event.value == "macaw" -> {
					eventOfMacaw.add(event)
				}
				event.value == "chainsaw" -> {
					eventOfChainsaw.add(event)
				}
				event.value == "vehicle" -> {
					eventOfVehicle.add(event)
				}
				event.value == "gunshot" -> {
					eventOfGunshot.add(event)
				}
				event.value == "trespasser" -> {
					eventOfTrespasser.add(event)
				}
				event.value == "other" -> {
					eventOfOther.add(event)
				}
				else -> eventOfMismatch.add(event)
			}
		}
		groupAll()
	}
	
	private fun groupAll() {
		if (eventOfAmazon.isNotEmpty()) {
			eventAll.addAll(listOf(eventOfAmazon))
		}
		
		if (eventOfMacaw.isNotEmpty()) {
			eventAll.addAll(listOf(eventOfMacaw))
		}
		
		if (eventOfChainsaw.isNotEmpty()) {
			eventAll.addAll(listOf(eventOfChainsaw))
		}
		
		if (eventOfVehicle.isNotEmpty()) {
			eventAll.addAll(listOf(eventOfVehicle))
		}
		
		if (eventOfGunshot.isNotEmpty()) {
			eventAll.addAll(listOf(eventOfGunshot))
		}
		
		if (eventOfTrespasser.isNotEmpty()) {
			eventAll.addAll(listOf(eventOfTrespasser))
		}
		
		if (eventOfOther.isNotEmpty()) {
			eventAll.addAll(listOf(eventOfOther))
		}
		
		if (eventOfOther.isNotEmpty()) {
			eventAll.addAll(listOf(eventOfOther))
		}
		
		if (eventOfMismatch.isNotEmpty()) {
			eventAll.addAll(listOf(eventOfMismatch))
		}
		addList(eventAll)
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
	
	fun itemsEvent(list: MutableList<Event>): MutableList<EventItem> {
		val itemsEvent = arrayListOf<EventItem>()
		list.forEach { event ->
			val state = eventDb.getEventState(event.event_guid)
			state?.let {
				val result = when (it) {
					ReviewEventFactory.confirmEvent -> EventItem.State.CONFIRM
					ReviewEventFactory.rejectEvent -> EventItem.State.REJECT
					else -> EventItem.State.NONE
				}
				itemsEvent.add(EventItem(event, result))
			} ?: run {
				itemsEvent.add(EventItem(event, EventItem.State.NONE))
			}
		}
		_alertsList = itemsEvent
		return itemsEvent
	}
	
	fun onEventReviewed(eventGuid: String, reviewValue: String) {
		val eventItem = _alertsList.firstOrNull { it.event.event_guid == eventGuid }
		if (eventItem != null) {
			eventItem.state = when (reviewValue) {
				ReviewEventFactory.confirmEvent -> EventItem.State.CONFIRM
				ReviewEventFactory.rejectEvent -> EventItem.State.REJECT
				else -> EventItem.State.NONE
			}
			_alertsList.replace(eventItem) { it.event.event_guid == eventGuid }
		}
		
		val arrayList = ArrayList<GuardianListDetail>()
		eventAll.forEach { events ->
			val num = events.size - numEvents(events)
			val item = itemsEvent(events)
			arrayList.add(GuardianListDetail(item, num))
		}
		_items.value = Result.Success(arrayList)
	}
	
	private fun addList(array: ArrayList<MutableList<Event>>) {
		val arrayList = ArrayList<GuardianListDetail>()
		array.forEach { events ->
			val num = events.size - numEvents(events)
			val item = itemsEvent(events)
			arrayList.add(GuardianListDetail(item, num))
		}
		_items.value = Result.Success(arrayList)
	}
	
	fun loadMoreEvents(guid: String, value: String, endAt: Date) {
		loading.postValue(StateLoading.LOADING)
		val requestFactory = EventsGuardianRequestFactory(guid, value, endAt, "measured_at", "DESC", LIMITS, 1)
		getMoreEvent.execute(object : DisposableSingleObserver<EventResponse>() {
			override fun onSuccess(t: EventResponse) {
				if (t.events !== null) {
				
					if (t.events!!.isEmpty()) {
						loading.postValue(StateLoading.NOT_LOADING)
						Toast.makeText(context, context.getString(R.string.not_have_event_more), Toast.LENGTH_SHORT).show()
					} else {
						val arrayList = ArrayList<GuardianListDetail>()
						eventAll.forEach { events ->
							var index = events.size
							
							val mainValue = arrayListOf<String>()
							events.distinctBy { it.value }.mapTo(mainValue, { it.value!! })
							
							if (mainValue.size == 1 && value == mainValue[0]) {
								t.events?.forEach { it ->
									events.add(index, it)
									index += 1
								}
							}
							val num = events.size - numEvents(events)
							val item = itemsEvent(events)
							arrayList.add(GuardianListDetail(item, num))
							_items.value = Result.Success(arrayList)
						}
						loading.postValue(StateLoading.NOT_LOADING)
					}
				}
			}
			
			override fun onError(e: Throwable) {
				_items.value = e.getResultError()
				loading.postValue(StateLoading.NOT_LOADING)
			}
			
		}, requestFactory)
	}
	
	companion object {
		const val LIMITS = 10
	}
}

data class GuardianListDetail(val events: MutableList<EventItem>, val unread: Int)

enum class StateLoading {
	LOADING, NOT_LOADING
}