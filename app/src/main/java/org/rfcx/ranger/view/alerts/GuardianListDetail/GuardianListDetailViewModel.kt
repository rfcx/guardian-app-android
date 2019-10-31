package org.rfcx.ranger.view.alerts.GuardianListDetail

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.data.remote.groupByGuardians.eventInGuardian.GetMoreEventInGuardian
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventsGuardianRequestFactory
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.util.EventItem
import org.rfcx.ranger.util.toEventItem
import java.util.*
import kotlin.collections.ArrayList

class GuardianListDetailViewModel(private val context: Context, private val eventDb: EventDb, private val getMoreEvent: GetMoreEventInGuardian) : ViewModel() {
	private val _items = MutableLiveData<Result<ArrayList<EventGroupByValue>>>()
	val items: LiveData<Result<ArrayList<EventGroupByValue>>> get() = _items
	
	var loading = MutableLiveData<StateLoading>()
	
	var eventsMap: MutableMap<String, MutableList<Event>> = mutableMapOf()
	
	fun getEventFromDatabase(guardianName: String) {
		val events = eventDb.getEvents().filter { it.guardianShortname == guardianName }
		events.forEach { event ->
			if (!eventsMap.containsKey(event.value)) {
				eventsMap[event.value!!] = mutableListOf(event)
			} else {
				eventsMap[event.value]?.add(event)
			}
		}
		val eventItem = ArrayList<EventGroupByValue>()
		eventsMap.forEach {
			eventItem.add(EventGroupByValue(makeListEventItem(it.value)))
		}
		_items.value = Result.Success(eventItem)
	}
	
	private fun makeListEventItem(list: MutableList<Event>): MutableList<EventItem> {
		val itemsEvent = arrayListOf<EventItem>()
		list.forEach { event ->
			itemsEvent.add(event.toEventItem(eventDb))
		}
		return itemsEvent
	}
	
	
	fun onEventReviewed(eventGuid: String, reviewValue: String) {
//		val eventItem = _alertsList.firstOrNull { it.event.event_guid == eventGuid }
//
//		if (eventItem != null) {
//			eventItem.state = when (reviewValue) {
//				ReviewEventFactory.confirmEvent -> EventItem.State.CONFIRM
//				ReviewEventFactory.rejectEvent -> EventItem.State.REJECT
//				else -> EventItem.State.NONE
//			}
//			_alertsList.replace(eventItem) { it.event.event_guid == eventGuid }
//		}
//
//		val arrayList = ArrayList<GuardianListDetail>()
//		eventAll.forEach { events ->
//			val num = events.size - numEvents(events)
//			val item = itemsEvent(events)
//			arrayList.add(GuardianListDetail(item, num))
//		}
//		_items.value = Result.Success(arrayList)
	}
	
	fun loadMoreEvents(guid: String, value: String, endAt: Date) {
		loading.postValue(StateLoading.LOADING)
		val requestFactory = EventsGuardianRequestFactory(guid, value, endAt, "begins_at", "DESC", LIMITS, 0, "alert")
//		getMoreEvent.execute(object : DisposableSingleObserver<EventResponse>() {
//			override fun onSuccess(t: EventResponse) {
//				if (t.events !== null) {
//
//					if (t.events!!.isEmpty()) {
//						loading.postValue(StateLoading.NOT_LOADING)
//						Toast.makeText(context, context.getString(R.string.not_have_event_more), Toast.LENGTH_SHORT).show()
//					} else {
//						val arrayList = ArrayList<GuardianListDetail>()
//						eventAll.forEach { events ->
//							var index = events.size
//
//							val mainValue = arrayListOf<String>()
//							events.distinctBy { it.value }.mapTo(mainValue, { it.value!! })
//
//							if (mainValue.size == 1 && value == mainValue[0]) {
//								t.events?.forEach { it ->
//									events.add(index, it)
//									index += 1
//								}
//							}
//							val num = events.size - numEvents(events)
//							val item = itemsEvent(events)
//							arrayList.add(GuardianListDetail(item, num))
//							_items.value = Result.Success(arrayList)
//						}
//						loading.postValue(StateLoading.NOT_LOADING)
//					}
//				}
//			}
//
//			override fun onError(e: Throwable) {
//				_items.value = e.getResultError()
//				loading.postValue(StateLoading.NOT_LOADING)
//			}
//
//		}, requestFactory)
	}
	
	companion object {
		const val LIMITS = 10
	}
}

data class EventGroupByValue(val events: MutableList<EventItem>) {
	fun numberOfUnread(eventDb: EventDb): Int {
		val read = events.fold(0) { acc, event ->
			val state = eventDb.getEventState(event.event.event_guid)
			if (state == ReviewEventFactory.confirmEvent || state == ReviewEventFactory.rejectEvent) acc + 1 else acc
		}
		return events.size - read
	}
}

enum class StateLoading {
	LOADING, NOT_LOADING
}