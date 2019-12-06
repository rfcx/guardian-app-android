package org.rfcx.ranger.view.alerts.GuardianListDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.rfcx.ranger.data.local.EventDb
import org.rfcx.ranger.data.remote.Result
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.util.EventItem
import org.rfcx.ranger.util.replace
import org.rfcx.ranger.util.toEventItem

class GuardianListDetailViewModel(private val eventDb: EventDb) : ViewModel() {
	
	private val _arrayEventGroup = MutableLiveData<Result<ArrayList<EventGroupByValue>>>()      // keep only 50 events
	val arrayEventGroup: LiveData<Result<ArrayList<EventGroupByValue>>> get() = _arrayEventGroup
	
	var arrayEventGroupMore = ArrayList<EventGroupByValue>() // keep when see older and use updete ui when review
	
	fun getEventFromDatabase(guardianName: String) {
		val events = eventDb.getEvents().filter { it.guardianName == guardianName }
		val eventItem = ArrayList<EventGroupByValue>()
		val eventsMap: MutableMap<String, MutableList<Event>> = mutableMapOf()
		
		events.forEach { event ->
			if (!eventsMap.containsKey(event.value)) {
				eventsMap[event.value] = mutableListOf(event)
			} else {
				eventsMap[event.value]?.add(event)
			}
		}
		eventsMap.forEach {
			eventItem.add(EventGroupByValue(makeListEventItem(it.value)))
		}
		arrayEventGroupMore = eventItem
		_arrayEventGroup.value = Result.Success(eventItem)
	}
	
	private fun makeListEventItem(list: MutableList<Event>): MutableList<EventItem> {
		val itemsEvent = arrayListOf<EventItem>()
		list.forEach { event ->
			itemsEvent.add(event.toEventItem(eventDb))
		}
		return itemsEvent
	}
	
	fun onEventReviewed(eventGuid: String, reviewValue: String) {
		arrayEventGroupMore.forEach { arr ->
			val arrayEvent = arr.events
			val updateEventItem = arrayEvent.firstOrNull { it.event.id == eventGuid }
			if (updateEventItem != null) {
				updateEventItem.state = when (reviewValue) {
					ReviewEventFactory.confirmEvent -> EventItem.State.CONFIRM
					ReviewEventFactory.rejectEvent -> EventItem.State.REJECT
					else -> EventItem.State.NONE
				}
				arrayEvent.replace(updateEventItem) { it.event.id == eventGuid }
			}
		}
		_arrayEventGroup.value = Result.Success(arrayEventGroupMore)
	}
}

data class EventGroupByValue(val events: MutableList<EventItem>) {
	fun numberOfUnread(eventDb: EventDb): Int {
		val read = events.fold(0) { acc, event ->
			val state = eventDb.getEventState(event.event.id)
			if (state == ReviewEventFactory.confirmEvent || state == ReviewEventFactory.rejectEvent) acc + 1 else acc
		}
		return events.size - read
	}
}