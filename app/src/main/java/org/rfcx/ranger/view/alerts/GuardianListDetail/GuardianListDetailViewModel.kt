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
import org.rfcx.ranger.entity.event.Event
import org.rfcx.ranger.entity.event.EventResponse
import org.rfcx.ranger.entity.event.EventsGuardianRequestFactory
import org.rfcx.ranger.entity.event.ReviewEventFactory
import org.rfcx.ranger.util.EventItem
import org.rfcx.ranger.util.getResultError
import org.rfcx.ranger.util.replace
import org.rfcx.ranger.util.toEventItem
import java.util.*
import kotlin.collections.ArrayList

class GuardianListDetailViewModel(private val context: Context, private val eventDb: EventDb, private val getMoreEvent: GetMoreEventInGuardian) : ViewModel() {
	
	private val _arrayEventGroup = MutableLiveData<Result<ArrayList<EventGroupByValue>>>()      // keep only 50 events
	val arrayEventGroup: LiveData<Result<ArrayList<EventGroupByValue>>> get() = _arrayEventGroup
	
	var arrayEventGroupMore = ArrayList<EventGroupByValue>() // keep when see older and use updete ui when review
	var loading = MutableLiveData<StateLoading>()
	
	fun getEventFromDatabase(guardianName: String) {
		val events = eventDb.getEvents().filter { it.guardianShortname == guardianName }
		val eventItem = ArrayList<EventGroupByValue>()
		val eventsMap: MutableMap<String, MutableList<Event>> = mutableMapOf()
		
		events.forEach { event ->
			if (!eventsMap.containsKey(event.value)) {
				eventsMap[event.value!!] = mutableListOf(event)
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
			val updateEventItem = arrayEvent.firstOrNull { it.event.event_guid == eventGuid }
			if (updateEventItem != null) {
				updateEventItem.state = when (reviewValue) {
					ReviewEventFactory.confirmEvent -> EventItem.State.CONFIRM
					ReviewEventFactory.rejectEvent -> EventItem.State.REJECT
					else -> EventItem.State.NONE
				}
				arrayEvent.replace(updateEventItem) { it.event.event_guid == eventGuid }
			}
		}
		_arrayEventGroup.value = Result.Success(arrayEventGroupMore)
	}
	
	fun loadMoreEvents(guid: String, value: String, endAt: Date) {
		loading.postValue(StateLoading.LOADING)
		val requestFactory = EventsGuardianRequestFactory(guid, value, endAt, "begins_at", "DESC", LIMITS, 0, "alert")
		getMoreEvent.execute(object : DisposableSingleObserver<EventResponse>() {
			override fun onSuccess(t: EventResponse) {
				if (t.events !== null) {
					
					if (t.events!!.isEmpty()) {
						loading.postValue(StateLoading.NOT_LOADING)
						Toast.makeText(context, context.getString(R.string.not_have_event_more), Toast.LENGTH_SHORT).show()
					} else {
						arrayEventGroupMore.forEach { arr ->
							val arrayEvent = arr.events
							
							if (arrayEvent[0].event.value == value) {
								var index = arrayEvent.size
								
								t.events?.forEach { it ->
									arrayEvent.add(index, it.toEventItem(eventDb))
									index += 1
								}
							}
							_arrayEventGroup.value = Result.Success(arrayEventGroupMore)
						}
						loading.postValue(StateLoading.NOT_LOADING)
					}
				}
			}
			
			override fun onError(e: Throwable) {
				_arrayEventGroup.value = e.getResultError()
				loading.postValue(StateLoading.NOT_LOADING)
			}
			
		}, requestFactory)
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